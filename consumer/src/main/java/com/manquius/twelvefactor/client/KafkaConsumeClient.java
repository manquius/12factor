package com.manquius.twelvefactor.client;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static java.util.Optional.ofNullable;

class KafkaConsumeClient implements ConsumeClient {

    private final Map<String, String> properties;
    private final Map<String, KafkaConsumer<String, String>> consumers;
    private static final Logger LOG = LoggerFactory.getLogger(ConsumeClient.class);

    public KafkaConsumeClient() throws ClientCreationException {
        properties = new HashMap<>();
        consumers = new HashMap<>();
        String protocol = ofNullable(System.getenv("KAFKA_SERVICE_PROTOCOL")).orElse("PLAINTEXT");
        String host = ofNullable(System.getenv("TWELVEFACTOR_KAFKA_SERVICE_HOST")).orElse("localhost");
        String port = ofNullable(System.getenv("TWELVEFACTOR_KAFKA_SERVICE_PORT")).orElse("9092");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, protocol + "://" + host + ":" + port);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "consumer-service");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "12factor");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    }

    @Override
    public List<String> consume(final String topic) throws ConsumeException {
        KafkaConsumer<String, String> client = getConsumer(topic);
        try {
            ConsumerRecords<String, String> records = client.poll(Duration.ofMillis(100));
            LOG.debug("Records read from " + topic + ": " + records.count());
            client.commitAsync();
            List<String> result = new ArrayList<>();
            records.forEach(v -> result.add(v.value()));
            return result;
        } catch(Exception e) {
            LOG.debug("Consumer failed. Generating a new one.", e);
            consumers.remove(topic);
            client.close();
            client = getConsumer(topic);
            ConsumerRecords<String, String> records = client.poll(Duration.ofMillis(100));
            LOG.debug("Records read from " + topic + ": " + records.count());
            client.commitAsync();
            List<String> result = new ArrayList<>();
            records.forEach(v -> result.add(v.value()));
            return result;
        }
    }

    private KafkaConsumer<String, String> getConsumer(final String topic) {
        if (!consumers.containsKey(topic)) {
            KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);
            consumer.subscribe(Arrays.asList(topic));
            consumers.put(topic, consumer);
        }
        return consumers.get(topic);
    }
}
