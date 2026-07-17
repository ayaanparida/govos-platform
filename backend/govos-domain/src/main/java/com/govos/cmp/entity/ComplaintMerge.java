package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintMergeStatus;
import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only merge metadata (CMP-001.6). Orchestration spans two complaint aggregates.
 */
@Entity
@Table(name = "cmp_complaint_merge", schema = "govos")
public class ComplaintMerge extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "surviving_complaint_id", nullable = false, updatable = false)
    private Complaint complaint;

    @NotNull
    @Column(name = "merged_complaint_id", nullable = false, updatable = false)
    private UUID mergedComplaintId;

    @NotNull
    @Column(name = "merged_by_user_id", nullable = false, updatable = false)
    private UUID mergedByUserId;

    @Column(name = "merge_reason", columnDefinition = "TEXT", updatable = false)
    private String mergeReason;

    @NotNull
    @PastOrPresent
    @Column(name = "merged_at", nullable = false, updatable = false)
    private Instant mergedAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, updatable = false)
    private ComplaintMergeStatus status = ComplaintMergeStatus.COMPLETED;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public UUID getMergedComplaintId() {
        return mergedComplaintId;
    }

    public void setMergedComplaintId(UUID mergedComplaintId) {
        this.mergedComplaintId = mergedComplaintId;
    }

    public UUID getMergedByUserId() {
        return mergedByUserId;
    }

    public void setMergedByUserId(UUID mergedByUserId) {
        this.mergedByUserId = mergedByUserId;
    }

    public String getMergeReason() {
        return mergeReason;
    }

    public void setMergeReason(String mergeReason) {
        this.mergeReason = mergeReason;
    }

    public Instant getMergedAt() {
        return mergedAt;
    }

    public void setMergedAt(Instant mergedAt) {
        this.mergedAt = mergedAt;
    }

    public ComplaintMergeStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintMergeStatus status) {
        this.status = status;
    }
}
