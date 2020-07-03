/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.clients;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * {@link ConsumeClient} implementation for a Redis Backing Service. It is the secondary adapter into {@link CircuitBreakerConsumeClient}, the default {@link ConsumeClient} implementation.
 * It can be configured using the following environment variables:
 * TWELVEFACTOR_REDIS_MASTER_SERVICE_HOST or TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_HOST default localhost
 * TWELVEFACTOR_REDIS_MASTER_SERVICE_PORT or TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_PORT default 6379
 */
class RedisConsumeClient implements ConsumeClient {

    private static final Logger LOG = LoggerFactory.getLogger(RedisConsumeClient.class);
    /**
     * Redis default port constant.
     */
    public static final int REDIS_DEFAULT_PORT = 6379;

    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> sync;
    private final RedisURI redisUri;

    public RedisConsumeClient() throws ClientCreationException {
        String host = System.getenv("TWELVEFACTOR_REDIS_MASTER_SERVICE_HOST");
        LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_MASTER_SERVICE_HOST: " + host);
        if(host == null){
            host = System.getenv("TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_HOST");
            LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_HOST: " + host);
        }
        if(host == null){
            LOG.debug("Defaulting Redis host configuration to: localhost");
            host = "localhost";
        }
        String portString = System.getenv("TWELVEFACTOR_REDIS_MASTER_SERVICE_PORT");
        LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_MASTER_SERVICE_PORT: " + portString);
        if(portString == null){
            portString = System.getenv("TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_PORT");
            LOG.debug("Reading Redis host configuration from TWELVEFACTOR_REDIS_ANNOUNCE_0_SERVICE_PORT: " + portString);
        }
        int port;
        if(portString == null){
            LOG.debug("Defaulting Redis port configuration to: " + REDIS_DEFAULT_PORT);
            port = REDIS_DEFAULT_PORT;
        } else {
            try {
                port = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                LOG.error("Configured Redis port is not a number: " + portString + ". Using default: " + REDIS_DEFAULT_PORT);
                port = REDIS_DEFAULT_PORT;
            }
        }
        redisUri = RedisURI.Builder.redis(host)
                .withPort(port)
                .build();
    }

    private RedisCommands<String, String> getCommands() {
        if (sync == null) {
            RedisClient redisClient = RedisClient.create(redisUri);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            sync = connection.sync();
        }
        return sync;
    }

    /**
     * Consume method implementation for Redis backing service.
     * @param topic from where the message will be consumed.
     * @throws ConsumeException if the message could not be consumed.
     * @return {@link List} of {@link String} messages
     */
    @Override
    public List<String> consume(String topic) throws ConsumeException {
        try {
            return getCommands().lrange("key", 0, -1);
        }catch (Exception e) {
            throw new ConsumeException("Error consuming from Redis.", e);
        }
    }

    /**
     * Closes Redis connection.
     */
    @Override
    public void close() {
        connection.close();
    }
}
