package com.govos.org.exception;

import java.util.UUID;

public class OrganizationNotFoundException extends OrgException {

    public OrganizationNotFoundException(UUID id) {
        super("Organization not found with id: " + id);
    }

    public OrganizationNotFoundException(String code) {
        super("Organization not found with code: " + code);
    }
}
