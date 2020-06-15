package com.manquius.twelvefactor.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;

import static java.util.Optional.ofNullable;

public class CircuitBreakerProduceClient implements ProduceClient {

    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreakerProduceClient.class);

    /**
     * Max attemps to produce before move to next level of failover.
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
     * Current attemps on failing productions.
     */
    private int attempts = 0;

    private final List<ProduceClient> clientAdapters;

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
     * @return boolean
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
                    attempts = 0;
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
}
