package com.govos.doc.storage.port;

public record MultipartPartResult(
        int partNumber,
        String etag,
        long partSize) {
}
