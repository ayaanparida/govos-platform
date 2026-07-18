package com.govos.srh.observability;

public class SearchObservationException extends RuntimeException {

    public SearchObservationException(String message) {
        super(message);
    }

    public SearchObservationException(String message, Throwable cause) {
        super(message, cause);
    }
}
