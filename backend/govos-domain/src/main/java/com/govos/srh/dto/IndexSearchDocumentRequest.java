package com.govos.srh.dto;

import java.util.UUID;

public record IndexSearchDocumentRequest(
        String indexCode,
        UUID documentId,
        String entityType,
        String documentJson
) {
}
