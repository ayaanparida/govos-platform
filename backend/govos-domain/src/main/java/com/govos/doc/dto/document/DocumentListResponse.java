package com.govos.doc.dto.document;

import java.util.List;

public record DocumentListResponse(
        List<DocumentSummaryResponse> documents,
        long totalElements,
        int totalPages,
        int pageNumber,
        int pageSize
) {
}
