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

public class CircuitBreakerConsumeClient implements ConsumeClient {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreakerConsumeClient.class);

    /**
     * Max attempts to consume before move to next level of failover.
     */
    private int maxAttempts;

    /**
     * Number of millis to wait before try to consume from level 0 again.
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
     * Current attempts on failing consuming.
     */
    private int attempts = 1;

    protected final List<ConsumeClient> clientAdapters;

    /**
     * CircuitBreakerProduceClient contructor.
     * @param adapters Ordered {@link List} of {@link ConsumeClient} that will connect to backing services. The default will be the first one, and it will call the next ones when the previous fails.
     *                 The max attempts to consume from a failing backing service is configured by CIRCUIT_BREAKER_MAX_ATTEMPTS environment variable. Default: 10.
     *                 The time it will continue consuming from a secondary backend before re-trying the previous one is configured by CIRCUIT_BREAKER_LEVEL_RESET_MS environment variable. Default:10000.
     */
    public CircuitBreakerConsumeClient(final List<ConsumeClient> adapters) {
        try {
            maxAttempts = Integer.parseInt(ofNullable(System.getenv("CIRCUIT_BREAKER_MAX_ATTEMPTS")).orElse("10"));
        } catch (NumberFormatException e) {
            maxAttempts = 10;
        }
        try {
            levelResetMillis = Long.parseLong(ofNullable(System.getenv("CIRCUIT_BREAKER_LEVEL_RESET_MS")).orElse("10000"));
        } catch (NumberFormatException e) {
            levelResetMillis = 10000;
        }
        this.clientAdapters = adapters;
    }

    /**
     * Consume method implementation with Circuit Breaker mechanism.
     * @param topic from where the message will be consumed.
     * @return
     * @throws ConsumeException if the message could not be produced.
     */
    @Override
    public List<String> consume(String topic) throws ConsumeException {
        List<String> messages = null;
        if (level > 0) {
            checkResetTime();
        }
        while (messages == null && level < clientAdapters.size()) {
            messages = tryToConsume(topic);
        }
        if (messages == null) {
            LOG.error("Message could not be received from any backend");
            throw new ConsumeException("Message could not be consumed from any backend");
        }
        return messages;
    }


    /**
     * Tries to produce a request to the current background service level.
     *
     * @return boolean
     */
    private List<String> tryToConsume(String topic) {
        List<String> messages = null;
        int currentLevel = level;
        while (messages == null && attempts <= maxAttempts && currentLevel == level) {
            try {
                ConsumeClient adapter = clientAdapters.get(level);
                messages = adapter.consume(topic);
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
        return messages;
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
                //Nothing to do
            }
        });
    }
}
