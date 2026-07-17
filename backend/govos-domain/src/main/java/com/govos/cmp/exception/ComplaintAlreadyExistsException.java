package com.govos.cmp.exception;

public class ComplaintAlreadyExistsException extends ComplaintException {

    public ComplaintAlreadyExistsException(String code) {
        super("Complaint code already exists: " + code);
    }
}
