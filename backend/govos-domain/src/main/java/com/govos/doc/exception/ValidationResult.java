package com.govos.doc.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ValidationResult {

    private final List<ValidationError> errors = new ArrayList<>();

    public void addError(String field, String message) {
        errors.add(new ValidationError(field, message));
    }

    public void addError(String field, String message, String code) {
        errors.add(new ValidationError(field, message, code));
    }

    public void addAll(ValidationResult other) {
        if (other != null) {
            errors.addAll(other.errors);
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public String summaryMessage() {
        if (errors.isEmpty()) {
            return "Validation succeeded";
        }
        return errors.stream()
                .map(error -> error.field() + ": " + error.message())
                .collect(Collectors.joining("; "));
    }

    public void throwIfInvalid() {
        if (!isValid()) {
            throw new DocumentValidationException(this);
        }
    }
}
