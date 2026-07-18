package com.govos.doc.exception;

import java.util.UUID;

public class ShareNotFoundException extends DocException {

    public ShareNotFoundException(UUID id) {
        super("Document share not found with id: " + id);
    }
}
