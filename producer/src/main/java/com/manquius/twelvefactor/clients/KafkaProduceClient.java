package com.manquius.twelvefactor.clients;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class KafkaProduceClient implements ProduceClient {


    private KafkaProducer<String, String> producer;
    private Callback callback = (metadata, exception) -> {
        //Nothing to do.
    };

    public KafkaProduceClient() throws ClientCreationException {
        Map<String, String> properties = new HashMap<>();

        String protocol = ofNullable(System.getenv("KAFKA_SERVICE_PROTOCOL")).orElse("PLAINTEXT");
        String host = ofNullable(System.getenv("TWELVEFACTOR_KAFKA_SERVICE_HOST")).orElse("localhost");
        String port = ofNullable(System.getenv("TWELVEFACTOR_KAFKA_SERVICE_PORT")).orElse("9092");
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, protocol + "://" + host + ":" + port);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-service");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        this.producer = new KafkaProducer(properties);
    }

    @Override
    public void produce(String topic, String message) throws ProduceException {
        try {
            final ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic,
                    message);
            producer.send(record, callback);
        } catch (Exception e) {
            throw new ProduceException("Error producing to Kafka", e);
        }
    }
}
