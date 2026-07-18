package com.govos.doc.storage.port;

import java.util.UUID;

public record MultipartUploadRequest(
        UUID documentId,
        String bucket,
        String key,
        String contentType) {
}
