package com.govos.doc.exception;

import java.util.UUID;

public class CategoryNotFoundException extends DocException {

    public CategoryNotFoundException(UUID id) {
        super("Document category not found with id: " + id);
    }
}
