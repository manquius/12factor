package com.manquius.twelvefactor.client;

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

class RabbitMQConsumeClient implements ConsumeClient {

    private Connection conn;

    RabbitMQConsumeClient() throws ClientCreationException {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String hostname = ofNullable(System.getenv("TWELVEFACTOR_RABBITMQ_SERVICE_HOST")).orElse("amqp://userName:password@hostName:portNumber/virtualHost");
            String port = ofNullable(System.getenv("TWELVEFACTOR_RABBITMQ_SERVICE_PORT")).orElse("amqp://userName:password@hostName:portNumber/virtualHost");

            factory.setUri("amqp://userName:password@hostName:portNumber/virtualHost");
            conn = factory.newConnection();
        } catch (URISyntaxException | NoSuchAlgorithmException | KeyManagementException | TimeoutException | IOException e) {
            throw new ClientCreationException("Error creating RabbitMQ client", e);
        }
    }

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
}
