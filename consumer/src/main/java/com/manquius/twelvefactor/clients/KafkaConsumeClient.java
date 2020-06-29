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

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

import static java.util.Optional.ofNullable;

/**
 * {@link ConsumeClient} implementation for a Kafka Backing Service. It is the primary adapter into {@link CircuitBreakerConsumeClient}, the default {@link ConsumeClient} implementation.
 * It can be configured using the following environment variables:
 * KAFKA_SERVICE_PROTOCOL default PLAINTEXT
 * TWELVEFACTOR_KAFKA_SERVICE_HOST default localhost
 * TWELVEFACTOR_KAFKA_SERVICE_PORT default 9092
 */
class KafkaConsumeClient implements ConsumeClient {

    private final Map<String, String> properties;
    private final Map<String, KafkaConsumer<String, String>> consumers;
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumeClient.class);

    /**
     * KafkaConsumeClient constructor
     *  It can be configured using the following environment variables:
     *  KAFKA_SERVICE_PROTOCOL default PLAINTEXT
     *  TWELVEFACTOR_KAFKA_SERVICE_HOST default localhost
     *  TWELVEFACTOR_KAFKA_SERVICE_PORT default 9092
     * @throws ClientCreationException
     */
    public KafkaConsumeClient() throws ClientCreationException {
        try {
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
        }catch (Exception e) {
            throw new ClientCreationException("Error creating KafkaClient", e);
        }

    }

    /**
     * Consume method implementation for Kafka backing service.
     * @param topic from where the message will be consumed.
     * @throws ConsumeException if the message could not be consumed.
     * @return {@link List} of {@link String} messages
     */
    @Override
    public List<String> consume(final String topic) {
        KafkaConsumer<String, String> client = getConsumer(topic);
        try {
            return TryToConsume(topic, client);
        } catch(Exception e) {
            LOG.debug("Consumer failed. Generating a new one.", e);
            consumers.remove(topic);
            client.close();
            client = getConsumer(topic);
            return TryToConsume(topic, client);
        }
    }

    private List<String> TryToConsume(String topic, KafkaConsumer<String, String> client) {
        ConsumerRecords<String, String> records = client.poll(Duration.ofMillis(100));
        LOG.debug("Records read from " + topic + ": " + records.count());
        client.commitAsync();
        List<String> result = new ArrayList<>();
        records.forEach(v -> result.add(v.value()));
        return result;
    }

    private KafkaConsumer<String, String> getConsumer(final String topic) {
        if (!consumers.containsKey(topic)) {
            KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);
            consumer.subscribe(Collections.singletonList(topic));
            consumers.put(topic, consumer);
        }
        return consumers.get(topic);
    }

    /**
     * Closes kafka producer.
     */
    @Override
    public void close() {
        consumers.values().forEach(KafkaConsumer::close);
    }
}
