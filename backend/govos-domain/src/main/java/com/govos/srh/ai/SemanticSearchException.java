package com.govos.srh.ai;

import com.govos.srh.exception.SearchException;

public class SemanticSearchException extends SearchException {

    public SemanticSearchException(String message) {
        super(message);
    }

    public SemanticSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
