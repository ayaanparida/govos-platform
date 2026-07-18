package com.govos.idm.exception;

import java.util.UUID;

public class AssignmentNotFoundException extends IdmException {

    public AssignmentNotFoundException(String type, UUID id) {
        super(type + " assignment not found with id: " + id);
    }
}
