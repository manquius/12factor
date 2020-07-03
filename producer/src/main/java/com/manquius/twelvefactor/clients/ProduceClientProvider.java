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

import java.util.ArrayList;
import java.util.List;
import static java.util.Optional.ofNullable;

/**
 * {@link ProduceClient} provider.
 * It can be configured using PRODUCE_CLIENT environment variable. Default: PULSAR/REDIS.
 * Allowed values: PULSAR, KAFKA, REDIS (non case sensitive)
 * When the value inclused slashes '/', it uses {@link CircuitBreakerProduceClient} implementation passing the different adapters in order.
 * If the configuration is invalid, defaults to KAFKA/REDIS.
 */
public class ProduceClientProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ProduceClientProvider.class);
    private ProduceClient adapter;

    /**
     * Main method to return {@link ProduceClient} implementation based on PRODUCE_CLIENT environment variable.
     *
     * @return {@link ProduceClient} implementation
     * @throws ClientCreationException if the Client could not be created
     */
    public ProduceClient getClient() throws ClientCreationException {
        if (null == adapter) {
            final String clientString = ofNullable(System.getenv("PRODUCE_CLIENT")).orElse("PULSAR/REDIS");
            if (clientString.contains("/")) {
                List<ProduceClient> adapters = new ArrayList<>();
                for (String adapterName : clientString.split("/")) {
                    adapters.add(Adapter.getClientByName(adapterName));
                }
                adapter = new CircuitBreakerProduceClient(adapters);
            } else {
                adapter = Adapter.getClientByName(clientString);
            }
        }
        return adapter;
    }

    enum Adapter {
        KAFKA,
        REDIS,
        PULSAR;

        private static ProduceClient getClientByName(final String clientString) throws ClientCreationException {
            try {
                switch (Adapter.valueOf(clientString.toUpperCase())) {
                    case PULSAR:
                        return new PulsarProduceClient();
                    case KAFKA:
                        return new KafkaProduceClient();
                    case REDIS:
                        return new RedisProduceClient();
                    default:
                        throw new ClientCreationException("Invalid PRODUCE_CLIENT value: " + clientString);
                }
            } catch (IllegalArgumentException e) {
                throw new ClientCreationException("Invalid PRODUCE_CLIENT value: " + clientString);
            }
        }
    }
}
