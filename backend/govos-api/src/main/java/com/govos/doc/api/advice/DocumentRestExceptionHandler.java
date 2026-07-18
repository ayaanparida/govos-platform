package com.govos.doc.api.advice;

import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.response.ValidationError;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.exception.CategoryNotFoundException;
import com.govos.doc.exception.DocException;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.exception.MetadataNotFoundException;
import com.govos.doc.exception.RetentionPolicyNotFoundException;
import com.govos.doc.exception.ShareNotFoundException;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.exception.VersionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(basePackages = "com.govos.doc.api")
public class DocumentRestExceptionHandler {

    @ExceptionHandler({
            DocumentNotFoundException.class,
            FolderNotFoundException.class,
            CategoryNotFoundException.class,
            VersionNotFoundException.class,
            MetadataNotFoundException.class,
            RetentionPolicyNotFoundException.class,
            ShareNotFoundException.class,
            StorageProviderNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(DocException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request, null);
    }

    @ExceptionHandler(DocumentValidationException.class)
    public ResponseEntity<ErrorResponse> handleDocumentValidation(
            DocumentValidationException ex,
            HttpServletRequest request) {
        List<ValidationError> errors = ex.getValidationResult().getErrors().stream()
                .map(DocumentRestExceptionHandler::toApiValidationError)
                .toList();
        HttpStatus status = isConflict(ex) ? HttpStatus.CONFLICT : HttpStatus.UNPROCESSABLE_ENTITY;
        String code = status == HttpStatus.CONFLICT ? "CONFLICT" : "DOCUMENT_VALIDATION_ERROR";
        return buildResponse(status, code, ex.getMessage(), request, errors.isEmpty() ? null : errors);
    }

    @ExceptionHandler(DocException.class)
    public ResponseEntity<ErrorResponse> handleDocException(DocException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "DOCUMENT_ERROR", ex.getMessage(), request, null);
    }

    private static boolean isConflict(DocumentValidationException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Optimistic lock conflict")) {
            return true;
        }
        return ex.getValidationResult().getErrors().stream()
                .map(com.govos.doc.exception.ValidationError::code)
                .anyMatch(code -> code != null && code.contains("DUPLICATE"));
    }

    private static ValidationError toApiValidationError(com.govos.doc.exception.ValidationError error) {
        return new ValidationError(error.field(), error.message(), error.code());
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<ValidationError> errors) {
        ErrorResponse body = ErrorResponse.of(
                code,
                message,
                request.getRequestURI(),
                RequestContextUtils.resolveRequestId(request),
                errors);
        return ResponseEntity.status(status).body(body);
    }
}
