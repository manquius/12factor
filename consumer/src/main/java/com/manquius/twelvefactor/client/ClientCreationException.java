package com.manquius.twelvefactor.client;

public class ClientCreationException extends Exception{
    
    ClientCreationException() {
    }

    ClientCreationException(String s) {
        super(s);
    }

    ClientCreationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    ClientCreationException(Throwable throwable) {
        super(throwable);
    }
}
