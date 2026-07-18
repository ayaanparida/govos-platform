package com.govos.doc.dto.metadata;

import java.util.UUID;

public record DocumentMetadataResponse(
        UUID id,
        String code,
        UUID documentId,
        UUID documentVersionId,
        String ocrText,
        String ocrLanguage,
        Double ocrConfidence,
        String extractedMetadata,
        String customAttributes,
        Integer pageCount,
        String languageDetected,
        Boolean watermarkApplied,
        Boolean active,
        Long version
) {
}
