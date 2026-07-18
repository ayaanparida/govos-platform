package com.govos.idm.exception;

public class DuplicateUsernameException extends IdmException {

    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
