package com.govos.doc.storage.port;

import java.time.Instant;

public record StorageObjectMetadata(
        String contentType,
        long contentLength,
        String checksum,
        Instant createdDate,
        Instant lastModified,
        String storageClass,
        String etag) {
}
