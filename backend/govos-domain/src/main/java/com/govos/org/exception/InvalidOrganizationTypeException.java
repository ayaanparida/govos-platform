package com.govos.org.exception;

public class InvalidOrganizationTypeException extends OrgException {

    public InvalidOrganizationTypeException(String type) {
        super("Invalid organization type — not found in MDM ORGANIZATION_TYPE: " + type);
    }
}
