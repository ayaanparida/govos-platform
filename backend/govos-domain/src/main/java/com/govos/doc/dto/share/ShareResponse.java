package com.govos.doc.dto.share;

import com.govos.doc.enums.ShareType;

import java.time.Instant;
import java.util.UUID;

public record ShareResponse(
        UUID id,
        String code,
        UUID documentId,
        ShareType shareType,
        UUID sharedWithUserId,
        UUID sharedWithRoleId,
        String sharedWithEmail,
        UUID createdById,
        Instant expiresAt,
        String permission,
        String publicLinkUrl,
        Instant signedUrlExpiresAt,
        Boolean active,
        Long version
) {
}
