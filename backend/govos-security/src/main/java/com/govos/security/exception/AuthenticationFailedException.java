package com.govos.security.exception;

public class AuthenticationFailedException extends SecurityException {

    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
