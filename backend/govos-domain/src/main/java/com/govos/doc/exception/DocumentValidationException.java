package com.govos.doc.exception;

public class DocumentValidationException extends DocException {

    private final ValidationResult validationResult;

    public DocumentValidationException(ValidationResult validationResult) {
        super(validationResult.summaryMessage());
        this.validationResult = validationResult;
    }

    public DocumentValidationException(String message) {
        super(message);
        this.validationResult = new ValidationResult();
        this.validationResult.addError("general", message);
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}
