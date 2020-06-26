package com.manquius.twelvefactor.clients;

import java.util.ArrayList;
import java.util.List;
import static java.util.Optional.ofNullable;

public class ProduceClientProvider {

    private ProduceClient adapter;

    public ProduceClient getClient() throws ClientCreationException {
        if (null == adapter) {
            final String clientString = ofNullable(System.getenv("PRODUCE_CLIENT")).orElse("PULSAR/REDIS");
            if (clientString.contains("/")) {
                List<ProduceClient> adapters = new ArrayList<>();
                for (String adapterName : clientString.split("/")) {
                    adapters.add(Adapter.getClientByName(adapterName));
                }
                adapter = new CircuitBreakerProduceClient(adapters);
            } else {
                adapter = Adapter.getClientByName(clientString);
            }
        }
        return adapter;
    }

    enum Adapter {
        KAFKA,
        REDIS,
        PULSAR;

        private static ProduceClient getClientByName(final String clientString) throws ClientCreationException {
            switch(Adapter.valueOf(clientString)) {
                case PULSAR:
                    return new PulsarProduceClient();
                case KAFKA:
                    return new KafkaProduceClient();
                case REDIS:
                    return new RedisProduceClient();
                default:
                    throw new ClientCreationException("Invalid PRODUCE_CLIENT value: " + clientString);
            }
        }
    }
}
