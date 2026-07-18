package com.govos.doc.exception;

import java.util.UUID;

public class MetadataNotFoundException extends DocException {

    public MetadataNotFoundException(UUID id) {
        super("Document metadata not found with id: " + id);
    }
}
