package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintStatus;
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
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only business status transition log (CMP-001.6).
 */
@Entity
@Table(name = "cmp_complaint_status_history", schema = "govos")
public class ComplaintStatusHistory extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false, updatable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30, updatable = false)
    private ComplaintStatus fromStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30, updatable = false)
    private ComplaintStatus toStatus;

    @NotNull
    @Column(name = "changed_by_user_id", nullable = false, updatable = false)
    private UUID changedByUserId;

    @Column(name = "reason", columnDefinition = "TEXT", updatable = false)
    private String reason;

    @Size(max = 100)
    @Column(name = "reason_key", length = 100, updatable = false)
    private String reasonKey;

    @NotNull
    @PastOrPresent
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ComplaintStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ComplaintStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ComplaintStatus toStatus) {
        this.toStatus = toStatus;
    }

    public UUID getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(UUID changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReasonKey() {
        return reasonKey;
    }

    public void setReasonKey(String reasonKey) {
        this.reasonKey = reasonKey;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
