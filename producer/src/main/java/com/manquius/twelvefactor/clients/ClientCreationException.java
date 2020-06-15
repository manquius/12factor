package com.manquius.twelvefactor.clients;

import org.apache.pulsar.client.api.PulsarClientException;

public class ClientCreationException extends Exception {

    public ClientCreationException() {
    }

    public ClientCreationException(String message) {
        super(message);
    }

    public ClientCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientCreationException(Throwable cause) {
        super(cause);
    }
}
