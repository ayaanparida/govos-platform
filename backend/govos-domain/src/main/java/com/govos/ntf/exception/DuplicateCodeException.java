package com.govos.ntf.exception;

public class DuplicateCodeException extends NtfException {

    public DuplicateCodeException(String entityType, String code) {
        super(entityType + " code already exists: " + code);
    }
}
