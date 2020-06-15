package com.manquius.twelvefactor.client;

import org.apache.pulsar.client.api.*;

import java.util.ArrayList;
import java.util.List;


class PulsarConsumeClient implements ConsumeClient {

    private final PulsarClient client;
    private final ConsumerBuilder<String> builder;

    public PulsarConsumeClient() throws ClientCreationException {
        String pulsarBrokerRootUrl = "pulsar://localhost:6650";
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
            Consumer<String> consumer = builder.topic(topic).subscribe();
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
