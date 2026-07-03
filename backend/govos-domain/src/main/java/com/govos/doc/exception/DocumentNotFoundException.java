package com.govos.doc.exception;

import java.util.UUID;

public class DocumentNotFoundException extends DocException {

    public DocumentNotFoundException(UUID id) {
        super("Document not found with id: " + id);
    }

    public DocumentNotFoundException(String code) {
        super("Document not found with code: " + code);
    }
}
