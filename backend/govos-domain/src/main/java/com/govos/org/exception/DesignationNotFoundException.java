package com.govos.org.exception;

import java.util.UUID;

public class DesignationNotFoundException extends OrgException {

    public DesignationNotFoundException(UUID id) {
        super("Designation not found with id: " + id);
    }

    public DesignationNotFoundException(String code) {
        super("Designation not found with code: " + code);
    }
}
