package com.govos.doc.exception;

import java.util.UUID;

public class DocumentTagNotFoundException extends DocException {

    public DocumentTagNotFoundException(UUID id) {
        super("Document tag not found with id: " + id);
    }

    public DocumentTagNotFoundException(String name) {
        super("Document tag not found with name: " + name);
    }
}
