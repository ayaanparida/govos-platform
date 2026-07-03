package com.govos.idm.exception;

public class DuplicateEmailException extends IdmException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
