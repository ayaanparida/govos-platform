package com.govos.doc.exception;

import java.util.UUID;

public class DocumentVersionNotFoundException extends DocException {

    public DocumentVersionNotFoundException(UUID id) {
        super("Document version not found with id: " + id);
    }

    public DocumentVersionNotFoundException(UUID documentId, Integer versionNumber) {
        super("Document version not found: document=" + documentId + ", version=" + versionNumber);
    }
}
