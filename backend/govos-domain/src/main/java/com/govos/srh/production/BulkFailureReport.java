package com.govos.srh.production;

import java.util.List;

public record BulkFailureReport(
        long successCount,
        long failureCount,
        List<String> failedDocumentIds,
        String indexName,
        String operation
) {
}
