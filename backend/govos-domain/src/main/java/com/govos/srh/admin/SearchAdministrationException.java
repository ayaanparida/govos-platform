package com.govos.srh.admin;

import com.govos.srh.exception.SearchException;

public class SearchAdministrationException extends SearchException {

    public SearchAdministrationException(String message) {
        super(message);
    }

    public SearchAdministrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
