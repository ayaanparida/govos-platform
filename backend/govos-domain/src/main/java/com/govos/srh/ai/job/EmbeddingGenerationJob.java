package com.govos.srh.ai.job;

import java.time.Instant;
import java.util.UUID;

public class EmbeddingGenerationJob {

    private final UUID jobId;
    private final int targetVersion;
    private volatile EmbeddingGenerationJobStatus status;
    private volatile int totalDocuments;
    private volatile int processedDocuments;
    private volatile int failedDocuments;
    private volatile Instant startedAt;
    private volatile Instant completedAt;
    private volatile String lastError;

    public EmbeddingGenerationJob(UUID jobId, int targetVersion) {
        this.jobId = jobId;
        this.targetVersion = targetVersion;
        this.status = EmbeddingGenerationJobStatus.PENDING;
    }

    public UUID getJobId() {
        return jobId;
    }

    public int getTargetVersion() {
        return targetVersion;
    }

    public EmbeddingGenerationJobStatus getStatus() {
        return status;
    }

    public void setStatus(EmbeddingGenerationJobStatus status) {
        this.status = status;
    }

    public int getTotalDocuments() {
        return totalDocuments;
    }

    public void setTotalDocuments(int totalDocuments) {
        this.totalDocuments = totalDocuments;
    }

    public int getProcessedDocuments() {
        return processedDocuments;
    }

    public void incrementProcessed() {
        this.processedDocuments++;
    }

    public int getFailedDocuments() {
        return failedDocuments;
    }

    public void incrementFailed() {
        this.failedDocuments++;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}
