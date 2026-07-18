package com.govos.org.exception;

import java.util.UUID;

public class DepartmentHierarchyNotFoundException extends OrgException {

    public DepartmentHierarchyNotFoundException(UUID id) {
        super("Department hierarchy not found with id: " + id);
    }
}
