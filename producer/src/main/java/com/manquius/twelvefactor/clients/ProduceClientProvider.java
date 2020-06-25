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
                    adapters.add(getClientByName(adapterName));
                }
                adapter = new CircuitBreakerProduceClient(adapters);
            } else {
                getClientByName(clientString);
            }
        }
        return adapter;
    }

    private ProduceClient getClientByName(String clientString) throws ClientCreationException {
        if(clientString.equalsIgnoreCase("KAFKA")) {
            adapter = new KafkaProduceClient();
        }
        if(clientString.equalsIgnoreCase("REDIS")) {
            adapter = new RedisProduceClient();
        }
        if(clientString.equalsIgnoreCase("PULSAR")) {
            adapter = new PulsarProduceClient();
        }
        return adapter;
    }
}
