package com.govos.org.exception;

import java.util.UUID;

public class UserOrganizationNotFoundException extends OrgException {

    public UserOrganizationNotFoundException(UUID id) {
        super("User-organization assignment not found with id: " + id);
    }
}
