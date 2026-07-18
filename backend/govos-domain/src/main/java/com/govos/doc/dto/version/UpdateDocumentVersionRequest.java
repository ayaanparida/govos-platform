package com.govos.doc.dto.version;

import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.VirusScanStatus;

public record UpdateDocumentVersionRequest(
        String versionLabel,
        VirusScanStatus virusScanStatus,
        OcrStatus ocrStatus,
        PreviewStatus previewStatus,
        DocumentVersionStatus versionStatus,
        String previewStorageKey,
        String thumbnailStorageKey,
        Long version
) {
}
