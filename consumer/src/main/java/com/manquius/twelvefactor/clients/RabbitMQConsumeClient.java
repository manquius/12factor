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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static java.util.Optional.ofNullable;

/**
 * {@link ConsumeClient} implementation for a RabbitMQ Backing Service.
 * It can be configured using the following environment variables:
 *  TWELVEFACTOR_RABBITMQ_SERVICE_HOST default localhost
 *  TWELVEFACTOR_RABBITMQ_SERVICE_PORT default 5672
 */
class RabbitMQConsumeClient implements ConsumeClient {

    private final Connection conn;

    /**
     * KafkaConsumeClient constructor
     *  It can be configured using the following environment variables:
     *  KAFKA_SERVICE_PROTOCOL default PLAINTEXT
     *  TWELVEFACTOR_RABBITMQ_SERVICE_HOST default localhost
     *  TWELVEFACTOR_RABBITMQ_SERVICE_PORT default 5672
     * @throws ClientCreationException
     */
    public RabbitMQConsumeClient() throws ClientCreationException {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String hostname = ofNullable(System.getenv("TWELVEFACTOR_RABBITMQ_SERVICE_HOST")).orElse("localhost");
            String port = ofNullable(System.getenv("TWELVEFACTOR_RABBITMQ_SERVICE_PORT")).orElse("5672");

            factory.setUri("amqp://userName:password@\" + hostname + \":" + port + "/virtualHost");
            conn = factory.newConnection();
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | TimeoutException | IOException e) {
            throw new ClientCreationException("Error creating RabbitMQ client", e);
        }
    }

    /**
     * Consume method implementation for RabbitMQ backing service.
     * @param topic from where the message will be consumed.
     * @throws ConsumeException if the message could not be consumed.
     * @return {@link List} of {@link String} messages
     */
    @Override
    public List<String> consume(String topic) throws ConsumeException {
        try {
            Channel channel = conn.createChannel();
            List<String> result = new ArrayList<>();
            while(channel.consumerCount(topic) >0){
                GetResponse response = channel.basicGet(topic, true);
                String message = Arrays.toString(response.getBody());
                result.add(message);
            }
            return result;
        } catch (IOException e) {
            throw new ConsumeException("Error consuming from RabbitMQ topic: " + topic, e);
        }
    }

    /**
     * Closes RabbitMQ connection.
     */
    @Override
    public void close() throws Exception {
        conn.close();
    }
}
