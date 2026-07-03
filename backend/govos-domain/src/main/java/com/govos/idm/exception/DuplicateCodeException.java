package com.govos.idm.exception;

public class DuplicateCodeException extends IdmException {

    public DuplicateCodeException(String entityType, String code) {
        super(entityType + " code already exists: " + code);
    }
}
