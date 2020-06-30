/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ProduceClient} that handles a Circuit Breaker mechanism among the provided alternatives.
 * It is the default {@link ProduceClient}.
 * It can be configured Using the following environment variables:
 * CIRCUIT_BREAKER_MAX_ATTEMPTS: Number of attempts to produce to one backing service before passing to the next one. Default: 10.
 * CIRCUIT_BREAKER_LEVEL_RESET_MS: Number of milliseconds to keep producing to a secondary backing service, before trying to produce to the previous one. Default: 10000.
 */
public class CircuitBreakerProduceClient implements ProduceClient {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreakerProduceClient.class);

    /**
     * Max attempts to produce before move to next level of failover.
     */
    private int maxAttempts;

    /**
     * Number of millis to wait before try to produce to level 0 again.
     */
    private long levelResetMillis;

    /**
     * Calendar.
     */
    private final Calendar cal = Calendar.getInstance();

    /**
     * Last time.
     */
    private final long lastResetTime = cal.getTimeInMillis();

    /**
     * Failover level.
     */
    private int level = 0;

    /**
     * Current attempts on failing productions.
     */
    private int attempts = 1;

    protected final List<ProduceClient> clientAdapters;

    /**
     * CircuitBreakerProduceClient contructor.
     * @param adapters Ordered {@link List} of {@link ProduceClient} that will connect to backing services. The default will be the first one, and it will call the next ones when the previous fails.
     *                 The max attempts to produce to a failing backing service is configured by CIRCUIT_BREAKER_MAX_ATTEMPTS environment variable. Default: 10.
     *                 The time it will continue producing to a secondary backend before re-trying the previous one is configured by CIRCUIT_BREAKER_LEVEL_RESET_MS environment variable. Default:10000.
     */
    public CircuitBreakerProduceClient(final List<ProduceClient> adapters) {
        try {
            maxAttempts = Integer.valueOf(ofNullable("CIRCUIT_BREAKER_MAX_ATTEMPTS").orElse("10"));
        } catch(NumberFormatException e) {
            maxAttempts = 10;
        }
        try {
        levelResetMillis = Long.valueOf(ofNullable("CIRCUIT_BREAKER_LEVEL_RESET_MS").orElse("10000"));
        } catch(NumberFormatException e) {
            levelResetMillis = 10000;
        }
        this.clientAdapters = adapters;
    }

    /**
     * Produce method implementation with Circuit Breaker mechanism.
     * @param topic where the message will be produced.
     * @param message to be produced in the topic
     * @throws ProduceException if the message could not be produced.
     */
    @Override
    public void produce(String topic, String message) throws ProduceException {
        boolean sent = false;
        if (level > 0) {
            checkResetTime();
        }
        while (!sent && level < clientAdapters.size()) {
            sent = tryToProduce(topic, message);
        }
        if (!sent) {
            LOG.error("Message could not be sent to any backend");
            throw new ProduceException("Message could not be sent to any backend: " + message);
        }
    }


    /**
     * Tries to produce a request to the current background service level.
     *
     * @return boolean indicating if the message was produced successfully to any backing service.
     */
    private boolean tryToProduce(String topic, String message) {
        boolean sent = false;
        int currentLevel = level;
        while (!sent && attempts <= maxAttempts && currentLevel == level) {
            try {
                ProduceClient adapter = clientAdapters.get(level);
                adapter.produce(topic, message);
                sent = true;
            } catch (Exception e) {
                if (attempts < maxAttempts) {
                    LOG.error("Error trying to produce. Attempt: " + attempts);
                    attempts++;
                } else {
                    attempts = 1;
                    level++;
                    LOG.error("All the attempts to produce failed, moving to next level.");
                }
            }
        }
        return sent;
    }

    /**
     * resets background level if time has passed.
     */
    private void checkResetTime() {
        if (lastResetTime + levelResetMillis > cal.getTimeInMillis()) {
            level = 0;
        }
    }

    /**
     * Closes all the Backing Services adapters
     */
    @Override
    public void close() {
        clientAdapters.forEach(client -> {
            try {
                client.close();
            } catch (Exception e) {
                LOG.warn("Client could not be closed.", e);
            }
        });
    }
}
