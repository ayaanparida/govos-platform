package com.govos.srh.engine;

public record BulkOperationResult(
        long successCount,
        long failureCount
) {
}
