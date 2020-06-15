package com.manquius.twelvefactor.client;

public class ConsumeClientProvider {

    private ConsumeClient redisConsumeClient;


    public ConsumeClient getClient() throws ClientCreationException {
        if(redisConsumeClient == null) {
            redisConsumeClient = new KafkaConsumeClient();
        }
        return redisConsumeClient;
    }
}
