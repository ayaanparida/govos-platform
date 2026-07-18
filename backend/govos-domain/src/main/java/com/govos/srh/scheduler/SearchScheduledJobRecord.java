package com.govos.srh.scheduler;

import java.time.Instant;
import java.util.UUID;

public class SearchScheduledJobRecord {

    private final UUID recordId;
    private final String jobName;
    private volatile SearchScheduledJobStatus status;
    private final Instant startedAt;
    private volatile Instant completedAt;
    private volatile long durationMs;
    private volatile String errorMessage;
    private volatile long documentsProcessed;
    private volatile int retryCount;

    public SearchScheduledJobRecord(UUID recordId, String jobName) {
        this.recordId = recordId;
        this.jobName = jobName;
        this.status = SearchScheduledJobStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public UUID getRecordId() {
        return recordId;
    }

    public String getJobName() {
        return jobName;
    }

    public SearchScheduledJobStatus getStatus() {
        return status;
    }

    public void setStatus(SearchScheduledJobStatus status) {
        this.status = status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getDocumentsProcessed() {
        return documentsProcessed;
    }

    public void setDocumentsProcessed(long documentsProcessed) {
        this.documentsProcessed = documentsProcessed;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
