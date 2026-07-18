package com.govos.srh.admin;

public record SearchIndexEngineStats(
        long documentCount,
        long deletedCount,
        Long storageSizeBytes
) {
}
