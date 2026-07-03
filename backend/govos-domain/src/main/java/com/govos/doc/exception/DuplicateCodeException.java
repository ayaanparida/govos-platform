package com.govos.doc.exception;

public class DuplicateCodeException extends DocException {

    public DuplicateCodeException(String entityType, String code) {
        super(entityType + " code already exists: " + code);
    }
}
