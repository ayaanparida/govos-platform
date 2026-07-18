package com.govos.doc.exception;

public record ValidationError(
        String field,
        String message,
        String code
) {

    public ValidationError(String field, String message) {
        this(field, message, "DOC_VALIDATION_ERROR");
    }
}
