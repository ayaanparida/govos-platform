package com.govos.doc.exception;

import java.util.UUID;

public class VersionNotFoundException extends DocException {

    public VersionNotFoundException(UUID id) {
        super("Document version not found with id: " + id);
    }
}
