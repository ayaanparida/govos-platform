package com.govos.srh.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Aggregate root for bulk search synchronization jobs (SRH-001).
 */
@Entity
@Table(name = "srh_search_sync_job", schema = "govos")
public class SearchSyncJob extends AuditableEntity {

    @Column(name = "job_name", nullable = false, length = 255)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private SearchJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_status", nullable = false, length = 30)
    private SearchJobStatus status = SearchJobStatus.PENDING;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "processed_count", nullable = false)
    private Long processedCount = 0L;

    @Column(name = "success_count", nullable = false)
    private Long successCount = 0L;

    @Column(name = "failure_count", nullable = false)
    private Long failureCount = 0L;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_index_id", nullable = false)
    private SearchIndex searchIndex;

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public SearchJobType getJobType() {
        return jobType;
    }

    public void setJobType(SearchJobType jobType) {
        this.jobType = jobType;
    }

    public SearchJobStatus getStatus() {
        return status;
    }

    public void setStatus(SearchJobStatus status) {
        this.status = status;
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

    public Long getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Long processedCount) {
        this.processedCount = processedCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public SearchIndex getSearchIndex() {
        return searchIndex;
    }

    public void setSearchIndex(SearchIndex searchIndex) {
        this.searchIndex = searchIndex;
    }
}
