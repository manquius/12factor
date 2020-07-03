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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

/**
 * {@link ConsumeClient} provider.
 * It can be configured using CONSUME_CLIENT environment variable. Default: KAFKA/REDIS.
 * Allowed values: Kafka, Redis, Pulsar (non case sensitive)
 * When the value inclused slashes '/', it uses {@link CircuitBreakerConsumeClient} implementation passing the different adapters in order.
 * If the configuration is invalid, defaults to KAFKA/REDIS.
 */
public class ConsumeClientProvider {

    private ConsumeClient adapter;

    private static final Logger LOG = LoggerFactory.getLogger(ConsumeClientProvider.class);


    /**
     * Main method to return {@link ConsumeClient} implementation based on CONSUME_CLIENT environment variable.
     * @return {@link ConsumeClient} implementation
     * @throws ClientCreationException if the Client could not be created
     */
    public ConsumeClient getClient() throws ClientCreationException {
        if (null == adapter) {
            final String clientString = ofNullable(System.getenv("CONSUME_CLIENT")).orElse("PULSAR");
            adapter = Adapter.getClientByName(clientString);
        }
        return adapter;
    }


    enum Adapter {
        KAFKA,
        REDIS,
        PULSAR;

        private static ConsumeClient getClientByName(final String clientString) throws ClientCreationException {
            try{
                switch(Adapter.valueOf(clientString.toUpperCase())) {
                    case PULSAR:
                        return new PulsarConsumeClient();
                    case KAFKA:
                        return new KafkaConsumeClient();
                    case REDIS:
                        return new RedisConsumeClient();
                    default:
                        throw new ClientCreationException("Invalid CONSUME_CLIENT value: " + clientString);
                }
            } catch (IllegalArgumentException e) {
                throw new ClientCreationException("Invalid CONSUME_CLIENT value: " + clientString);
            }
        }
    }
}
