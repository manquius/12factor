package com.manquius.twelvefactor.client;

import java.util.ArrayList;
import java.util.List;
import static java.util.Optional.ofNullable;

public class ConsumeClientProvider {

    private ConsumeClient adapter;

    public ConsumeClient getClient() throws ClientCreationException {
        if (null == adapter) {
            final String clientString = ofNullable(System.getenv("CONSUME_CLIENT")).orElse("PULSAR");
            adapter = getClientByName(clientString);
        }
        return adapter;
    }

    private ConsumeClient getClientByName(String clientString) throws ClientCreationException {
        if(clientString.equalsIgnoreCase("KAFKA")) {
            adapter = new KafkaConsumeClient();
        }
        if(clientString.equalsIgnoreCase("REDIS")) {
            adapter = new RedisConsumeClient();
        }
        if(clientString.equalsIgnoreCase("PULSAR")) {
            adapter = new PulsarConsumeClient();
        }
        return adapter;
    }
}
