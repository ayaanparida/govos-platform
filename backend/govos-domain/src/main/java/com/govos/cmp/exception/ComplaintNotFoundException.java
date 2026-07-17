package com.govos.cmp.exception;

import java.util.UUID;

public class ComplaintNotFoundException extends ComplaintException {

    public ComplaintNotFoundException(UUID id) {
        super("Complaint not found with id: " + id);
    }

    public ComplaintNotFoundException(String code) {
        super("Complaint not found with code: " + code);
    }
}
