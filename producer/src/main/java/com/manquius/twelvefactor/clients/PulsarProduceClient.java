/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.clients;

import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.ofNullable;

/**
 * {@link ProduceClient} implementation for Pulsar backing service.
 *
 */
public class PulsarProduceClient implements ProduceClient, AutoCloseable {

    private final PulsarClient client;
    private final ProducerBuilder<String> builder;
    private static final Logger LOG = LoggerFactory.getLogger(PulsarProduceClient.class);

    public PulsarProduceClient() throws ClientCreationException {
        String host = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_HOST")).orElse("localhost");
        String port = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_PORT_PULSAR")).orElse("6650");
        String protocol = ofNullable(System.getenv("PULSAR_PROTO")).orElse("pulsar");
        String pulsarBrokerRootUrl = protocol + "://" + host + ":" + port;
        try {
            client = PulsarClient.builder().serviceUrl(pulsarBrokerRootUrl).build();
            builder = client.newProducer(Schema.STRING);
        } catch (PulsarClientException e) {
            throw new ClientCreationException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void produce(final String topic, final String message) throws ProduceException {
        try(Producer<String> producer = builder.topic(topic).create()) {
            producer.send(message);
        } catch (PulsarClientException e) {
            throw new ProduceException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            client.close();
        } catch (PulsarClientException e) {
            LOG.warn("Error closing Pulsar Client", e);
        }
    }
}
