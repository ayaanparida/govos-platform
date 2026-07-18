package com.govos.doc.storage.port;

import java.time.Instant;

public record SignedUrlResult(
        String url,
        Instant expiresAt,
        String method) {
}
