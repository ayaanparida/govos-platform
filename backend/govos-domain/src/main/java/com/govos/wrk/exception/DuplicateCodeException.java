package com.govos.wrk.exception;

public class DuplicateCodeException extends WrkException {

    public DuplicateCodeException(String entityType, String code) {
        super(entityType + " code already exists: " + code);
    }
}
