package com.manquius.twelvefactor.client;

import static java.util.Optional.ofNullable;

public class ConsumeClientProvider {

    private ConsumeClient adapter;

    public ConsumeClient getClient() throws ClientCreationException {
        if (null == adapter) {
            final String clientString = ofNullable(System.getenv("CONSUME_CLIENT")).orElse("PULSAR");
            adapter = Adapter.getClientByName(clientString);
        }
        return adapter;
    }


    enum Adapter {
        KAFKA,
        REDIS,
        PULSAR;

        private static ConsumeClient getClientByName(final String clientString) throws ClientCreationException {
            switch(Adapter.valueOf(clientString)) {
                case PULSAR:
                    return new PulsarConsumeClient();
                case KAFKA:
                    return new KafkaConsumeClient();
                case REDIS:
                    return new RedisConsumeClient();
                default:
                    throw new ClientCreationException("Invalid CONSUME_CLIENT value: " + clientString);
            }
        }
    }
}
