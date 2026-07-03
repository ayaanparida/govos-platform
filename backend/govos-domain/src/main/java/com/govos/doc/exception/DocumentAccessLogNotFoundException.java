package com.govos.doc.exception;

import java.util.UUID;

public class DocumentAccessLogNotFoundException extends DocException {

    public DocumentAccessLogNotFoundException(UUID id) {
        super("Document access log not found with id: " + id);
    }
}
