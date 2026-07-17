package com.govos.security.exception;

public class PasswordPolicyException extends SecurityException {

    public PasswordPolicyException(String message) {
        super(message);
    }

    public PasswordPolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
