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

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * {@link ConsumeClient} implementation for a Pulsar Backing Service.
 */
class PulsarConsumeClient implements ConsumeClient {

    private static final Logger LOG = LoggerFactory.getLogger(PulsarConsumeClient.class);
    private final PulsarClient client;
    private final ConsumerBuilder<String> builder;

    /**
     * PulsarConsumeClient constructor
     * @throws ClientCreationException
     */
    public PulsarConsumeClient() throws ClientCreationException {
        String host = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_HOST")).orElse("localhost");
        String port = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_PORT_PULSAR")).orElse("6650");
        String protocol = ofNullable(System.getenv("PULSAR_PROTO")).orElse("pulsar");
        String pulsarBrokerRootUrl = protocol + "://" + host + ":" + port;
        try {
            client = PulsarClient.builder().serviceUrl(pulsarBrokerRootUrl).build();
            builder = client.newConsumer(Schema.STRING);
        } catch (PulsarClientException e) {
            throw new ClientCreationException("Error creating pulsar client.", e);
        }
    }

    /**
     * Consume method implementation for Pulsar backing service.
     * @param topic from where the message will be consumed.
     * @throws ConsumeException if the message could not be consumed.
     * @return {@link List} of {@link String} messages
     */
    @Override
    public List<String> consume(final String topic) throws ConsumeException {
        try{
            Consumer<String> consumer = builder.topic(topic).subscriptionName("12factor").subscribe();
            Messages<String> messages = consumer.batchReceive();
            consumer.acknowledge(messages);
            consumer.close();
            List<String> result = new ArrayList<>();
            messages.forEach(stringMessage -> result.add(stringMessage.getValue()));
            return result;
        } catch (PulsarClientException e) {
            throw new ConsumeException("Error consuming from Pulsar Topic: " + topic, e);
        }
    }

    /**
     * Closes Pulsar client.
     */
    @Override
    public void close() {
        try {
            client.close();
        } catch (Exception e) {
            LOG.warn("PulsarConsumeClient could not be closed");
        }
    }
}
