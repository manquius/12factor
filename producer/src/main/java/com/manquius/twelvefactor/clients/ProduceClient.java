package com.manquius.twelvefactor.clients;

public interface ProduceClient {

    void produce(String topic, String message) throws ProduceException;

}
