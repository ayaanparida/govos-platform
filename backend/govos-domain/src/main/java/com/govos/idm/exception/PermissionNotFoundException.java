package com.govos.idm.exception;

import java.util.UUID;

public class PermissionNotFoundException extends IdmException {

    public PermissionNotFoundException(UUID id) {
        super("Permission not found with id: " + id);
    }

    public PermissionNotFoundException(String code) {
        super("Permission not found with code: " + code);
    }
}
