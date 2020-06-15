package com.manquius.twelvefactor.clients;

import org.apache.pulsar.client.api.PulsarClientException;

public class ProduceException extends Exception {
    public ProduceException() {
    }

    public ProduceException(String message) {
        super(message);
    }

    public ProduceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProduceException(Throwable cause) {
        super(cause);
    }
}
