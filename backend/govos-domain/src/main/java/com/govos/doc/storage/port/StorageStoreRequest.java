package com.govos.doc.storage.port;

import java.util.UUID;

public record StorageStoreRequest(
        UUID documentId,
        String bucket,
        String key,
        String contentType,
        long contentLength,
        String checksum) {
}
