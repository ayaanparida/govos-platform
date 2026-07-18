package com.govos.doc.dto.version;

import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;

import java.time.Instant;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        String code,
        UUID documentId,
        Integer versionNumber,
        String versionLabel,
        String checksum,
        UUID storageProviderId,
        String storageObjectKey,
        String previewStorageKey,
        String thumbnailStorageKey,
        String mimeType,
        String originalFilename,
        Long sizeBytes,
        UUID uploadedById,
        Instant uploadedAt,
        VirusScanStatus virusScanStatus,
        OcrStatus ocrStatus,
        PreviewStatus previewStatus,
        DocumentVersionStatus versionStatus,
        Boolean immutable,
        Boolean active,
        Long version
) {
}
