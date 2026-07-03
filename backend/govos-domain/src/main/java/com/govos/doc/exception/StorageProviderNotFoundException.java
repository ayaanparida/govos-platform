package com.govos.doc.exception;

import java.util.UUID;

public class StorageProviderNotFoundException extends DocException {

    public StorageProviderNotFoundException(UUID id) {
        super("Storage provider not found with id: " + id);
    }

    public StorageProviderNotFoundException(String code) {
        super("Storage provider not found with code: " + code);
    }
}
