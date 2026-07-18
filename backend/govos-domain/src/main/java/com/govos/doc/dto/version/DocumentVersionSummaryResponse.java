package com.govos.doc.dto.version;

import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.VirusScanStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionSummaryResponse(
        UUID id,
        UUID documentId,
        Integer versionNumber,
        String versionLabel,
        String mimeType,
        String originalFilename,
        Long sizeBytes,
        DocumentVersionStatus versionStatus,
        VirusScanStatus virusScanStatus,
        Instant uploadedAt
) {
}
