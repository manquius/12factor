package com.manquius.twelvefactor.client;

public class ConsumeException extends Exception {
    public ConsumeException() {
    }

    public ConsumeException(String s) {
        super(s);
    }

    public ConsumeException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ConsumeException(Throwable throwable) {
        super(throwable);
    }
}
