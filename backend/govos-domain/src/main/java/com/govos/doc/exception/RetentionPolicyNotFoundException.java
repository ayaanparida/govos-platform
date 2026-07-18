package com.govos.doc.exception;

import java.util.UUID;

public class RetentionPolicyNotFoundException extends DocException {

    public RetentionPolicyNotFoundException(UUID id) {
        super("Retention policy not found with id: " + id);
    }
}
