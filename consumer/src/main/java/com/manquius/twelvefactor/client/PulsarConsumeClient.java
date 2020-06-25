package com.manquius.twelvefactor.client;

import org.apache.pulsar.client.api.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;


class PulsarConsumeClient implements ConsumeClient {

    private final PulsarClient client;
    private final ConsumerBuilder<String> builder;

    public PulsarConsumeClient() throws ClientCreationException {
        String host = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_HOST")).orElse("localhost");
        String port = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_PORT_PULSAR")).orElse("6650");
        String pulsarBrokerRootUrl = "pulsar+ssl://" + host + ":" + port;
        try {
            client = PulsarClient.builder().serviceUrl(pulsarBrokerRootUrl).build();
            builder = client.newConsumer(Schema.STRING);
        } catch (PulsarClientException e) {
            throw new ClientCreationException(e);
        }
    }

    @Override
    public List<String> consume(String topic) throws ConsumeException {
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
}
