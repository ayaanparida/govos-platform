package com.govos.org.exception;

public class DuplicateEmployeeNumberException extends OrgException {

    public DuplicateEmployeeNumberException(String employeeNumber) {
        super("Employee number already exists: " + employeeNumber);
    }
}
