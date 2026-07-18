package com.govos.srh.ai.migration;

import java.time.Instant;
import java.util.UUID;

public class EmbeddingMigrationStatus {

    private final UUID migrationId;
    private final int sourceVersion;
    private final int targetVersion;
    private EmbeddingMigrationPhase phase;
    private final Instant startedAt;
    private Instant completedAt;
    private int processedDocuments;
    private int failedDocuments;
    private String lastError;

    public EmbeddingMigrationStatus(
            UUID migrationId,
            int sourceVersion,
            int targetVersion,
            EmbeddingMigrationPhase phase,
            Instant startedAt,
            Instant completedAt,
            int processedDocuments,
            int failedDocuments) {
        this.migrationId = migrationId;
        this.sourceVersion = sourceVersion;
        this.targetVersion = targetVersion;
        this.phase = phase;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.processedDocuments = processedDocuments;
        this.failedDocuments = failedDocuments;
    }

    public UUID migrationId() {
        return migrationId;
    }

    public int sourceVersion() {
        return sourceVersion;
    }

    public int targetVersion() {
        return targetVersion;
    }

    public EmbeddingMigrationPhase phase() {
        return phase;
    }

    public void setPhase(EmbeddingMigrationPhase phase) {
        this.phase = phase;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public int processedDocuments() {
        return processedDocuments;
    }

    public void setProcessedDocuments(int processedDocuments) {
        this.processedDocuments = processedDocuments;
    }

    public int failedDocuments() {
        return failedDocuments;
    }

    public void setFailedDocuments(int failedDocuments) {
        this.failedDocuments = failedDocuments;
    }

    public String lastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
