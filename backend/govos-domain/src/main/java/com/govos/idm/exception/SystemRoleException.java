package com.govos.idm.exception;

public class SystemRoleException extends IdmException {

    public SystemRoleException(String operation) {
        super("System role cannot be " + operation);
    }
}
