package com.govos.doc.dto.metadata;

public record UpdateDocumentMetadataRequest(
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
