package com.govos.org.exception;

import java.util.UUID;

public class DepartmentNotFoundException extends OrgException {

    public DepartmentNotFoundException(UUID id) {
        super("Department not found with id: " + id);
    }
}
