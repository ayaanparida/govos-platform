package com.govos.srh.ai.migration;

public enum EmbeddingMigrationPhase {
    RUNNING,
    COMPLETED,
    FAILED,
    ROLLED_BACK,
    REINDEXED
}
