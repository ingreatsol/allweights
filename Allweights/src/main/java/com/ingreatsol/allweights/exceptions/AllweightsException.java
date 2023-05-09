package com.ingreatsol.allweights.exceptions;

public class AllweightsException extends Exception {
    public AllweightsException() {
    }

    public AllweightsException(String message) {
        super(message);
    }

    public AllweightsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AllweightsException(Throwable cause) {
        super(cause);
    }
}
