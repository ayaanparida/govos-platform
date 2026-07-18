package com.govos.org.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends OrgException {

    public EmployeeNotFoundException(UUID id) {
        super("Employee not found with id: " + id);
    }

    public EmployeeNotFoundException(String employeeNumber) {
        super("Employee not found with number: " + employeeNumber);
    }
}
