package com.manquius.twelvefactor.clients;

import org.apache.pulsar.client.api.*;

import static java.util.Optional.ofNullable;

public class PulsarProduceClient implements ProduceClient {

    private final PulsarClient client;
    private final ProducerBuilder<String> builder;

    public PulsarProduceClient() throws ClientCreationException {
        String host = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_HOST")).orElse("localhost");
        String port = ofNullable(System.getenv("TWELVEFACTOR_PULSAR_PROXY_SERVICE_PORT_PULSAR")).orElse("6650");
        String pulsarBrokerRootUrl = "pulsar+ssl://" + host + ":" + port;
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
