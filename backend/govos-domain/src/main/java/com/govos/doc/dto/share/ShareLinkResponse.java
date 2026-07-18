package com.govos.doc.dto.share;

import java.time.Instant;
import java.util.UUID;

public record ShareLinkResponse(
        UUID shareId,
        UUID documentId,
        String publicLinkUrl,
        Instant signedUrlExpiresAt,
        Instant expiresAt,
        String permission
) {
}
