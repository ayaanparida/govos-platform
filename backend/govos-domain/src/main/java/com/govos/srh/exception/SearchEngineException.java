package com.govos.srh.exception;

public class SearchEngineException extends SearchException {

    public SearchEngineException(String message) {
        super(message);
    }

    public SearchEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
