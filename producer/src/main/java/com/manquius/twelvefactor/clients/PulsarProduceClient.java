package com.manquius.twelvefactor.clients;

import org.apache.pulsar.client.api.*;

public class PulsarProduceClient implements ProduceClient {

    private final PulsarClient client;
    private final ProducerBuilder<String> builder;

    public PulsarProduceClient() throws ClientCreationException {
        String pulsarBrokerRootUrl = "pulsar://localhost:6650";
        try {
            client = PulsarClient.builder().serviceUrl(pulsarBrokerRootUrl).build();
            builder = client.newProducer(Schema.STRING);
        } catch (PulsarClientException e) {
            throw new ClientCreationException(e);
        }
    }

    @Override
    public void produce(String topic, String message) throws ProduceException {
        try(Producer<String> producer = builder.topic(topic).create()) {
            producer.send(message);
        } catch (PulsarClientException e) {
            throw new ProduceException(e);
        }
    }
}
