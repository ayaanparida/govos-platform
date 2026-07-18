package com.govos.doc.storage.health;

import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageHealthStatus;
import com.govos.doc.storage.service.DocumentStorageService;
import org.springframework.stereotype.Component;

@Component
public class StorageHealthIndicator {

    private final DocumentStorageService documentStorageService;

    public StorageHealthIndicator(DocumentStorageService documentStorageService) {
        this.documentStorageService = documentStorageService;
    }

    public StorageHealthStatus status() {
        StorageHealth health = documentStorageService.health();
        return health.status();
    }

    public StorageHealth details() {
        return documentStorageService.health();
    }
}
