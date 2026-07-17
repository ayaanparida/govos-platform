package com.govos.cmp.exception;

public class ComplaintCategoryException extends ComplaintException {

    public ComplaintCategoryException(String message) {
        super(message);
    }

    public ComplaintCategoryException(String type, String key) {
        super("Invalid complaint category — not found in MDM " + type + ": " + key);
    }
}
