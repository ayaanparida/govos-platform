package com.govos.org.exception;

public class DuplicateCodeException extends OrgException {

    public DuplicateCodeException(String entityType, String code) {
        super(entityType + " code already exists: " + code);
    }
}
