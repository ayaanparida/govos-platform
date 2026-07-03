package com.govos.doc.exception;

import java.util.UUID;

public class DocumentTagMappingNotFoundException extends DocException {

    public DocumentTagMappingNotFoundException(UUID id) {
        super("Document tag mapping not found with id: " + id);
    }
}
