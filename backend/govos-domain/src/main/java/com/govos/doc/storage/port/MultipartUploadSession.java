package com.govos.doc.storage.port;

public record MultipartUploadSession(
        String uploadId,
        StorageObjectRef objectRef,
        String contentType) {
}
