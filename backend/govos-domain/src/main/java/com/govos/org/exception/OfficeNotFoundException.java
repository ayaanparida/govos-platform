package com.govos.org.exception;

import java.util.UUID;

public class OfficeNotFoundException extends OrgException {

    public OfficeNotFoundException(UUID id) {
        super("Office not found with id: " + id);
    }
}
