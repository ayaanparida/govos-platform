package com.govos.idm.exception;

import java.util.UUID;

public class RoleNotFoundException extends IdmException {

    public RoleNotFoundException(UUID id) {
        super("Role not found with id: " + id);
    }

    public RoleNotFoundException(String code) {
        super("Role not found with code: " + code);
    }
}
