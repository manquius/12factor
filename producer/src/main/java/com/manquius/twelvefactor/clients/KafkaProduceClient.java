/*
 * Copyright (c) 2020. Fernando Ezequiel Mancuso (Manquius).
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.manquius.twelvefactor.clients;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

/**
 * {@link ProduceClient} implementation for a Kafka Backing Service. It is the primary adapter into {@link CircuitBreakerProduceClient}, the default {@link ProduceClient} implementation.
 * It can be configured using the following environment variables:
 * KAFKA_SERVICE_PROTOCOL default PLAINTEXT
 * TWELVEFACTOR_KAFKA_SERVICE_HOST default localhost
 * TWELVEFACTOR_KAFKA_SERVICE_PORT default 9092
 */
public class KafkaProduceClient implements ProduceClient {


    private final KafkaProducer<String, String> producer;
    private final Callback callback = (metadata, exception) -> {
        //Nothing to do.
    };

    /**
     * KafkaProduceClient constructor
     *  It can be configured using the following environment variables:
     *  KAFKA_SERVICE_PROTOCOL default PLAINTEXT
     *  TWELVEFACTOR_KAFKA_SERVICE_HOST default localhost
     *  TWELVEFACTOR_KAFKA_SERVICE_PORT default 9092
     * @throws ClientCreationException
     */
    public KafkaProduceClient() throws ClientCreationException {
        try {
            Map<String, String> properties = new HashMap<>();

            String protocol = ofNullable(System.getenv("KAFKA_SERVICE_PROTOCOL")).orElse("PLAINTEXT");
            String host = ofNullable(System.getenv("TWELVEFACTOR_KAFKA_SERVICE_HOST")).orElse("localhost");
            String port = ofNullable(System.getenv("TWELVEFACTOR_KAFKA_SERVICE_PORT")).orElse("9092");
            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, protocol + "://" + host + ":" + port);
            properties.put(ProducerConfig.CLIENT_ID_CONFIG, "producer-service");
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            this.producer = new KafkaProducer(properties);
        }catch(Exception e ){
            throw new ClientCreationException("Error creating Kafka client", e);
        }
    }

    /**
     * Produce method implementation for Kafka backing service.
     * @param topic where the message will be produced.
     * @param message to be produced in the topic
     * @throws ProduceException if the message could not be produced.
     */
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

    /**
     * Closes kafka producer.
     */
    @Override
    public void close() {
        this.producer.close();
    }
}
