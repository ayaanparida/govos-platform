package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintEscalationLevel;
import com.govos.cmp.enums.ComplaintEscalationReason;
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
 * Append-only escalation event log (CMP-001.6).
 */
@Entity
@Table(name = "cmp_complaint_escalation", schema = "govos")
public class ComplaintEscalation extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false, updatable = false)
    private Complaint complaint;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_level", nullable = false, length = 30, updatable = false)
    private ComplaintEscalationLevel escalationLevel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_reason", nullable = false, length = 30, updatable = false)
    private ComplaintEscalationReason escalationReason;

    @NotNull
    @Column(name = "escalated_by_user_id", nullable = false, updatable = false)
    private UUID escalatedByUserId;

    @Column(name = "escalated_to_user_id", updatable = false)
    private UUID escalatedToUserId;

    @Column(name = "escalated_to_department_id", updatable = false)
    private UUID escalatedToDepartmentId;

    @Column(name = "remarks", columnDefinition = "TEXT", updatable = false)
    private String remarks;

    @NotNull
    @PastOrPresent
    @Column(name = "escalated_at", nullable = false, updatable = false)
    private Instant escalatedAt;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintEscalationLevel getEscalationLevel() {
        return escalationLevel;
    }

    public void setEscalationLevel(ComplaintEscalationLevel escalationLevel) {
        this.escalationLevel = escalationLevel;
    }

    public ComplaintEscalationReason getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(ComplaintEscalationReason escalationReason) {
        this.escalationReason = escalationReason;
    }

    public UUID getEscalatedByUserId() {
        return escalatedByUserId;
    }

    public void setEscalatedByUserId(UUID escalatedByUserId) {
        this.escalatedByUserId = escalatedByUserId;
    }

    public UUID getEscalatedToUserId() {
        return escalatedToUserId;
    }

    public void setEscalatedToUserId(UUID escalatedToUserId) {
        this.escalatedToUserId = escalatedToUserId;
    }

    public UUID getEscalatedToDepartmentId() {
        return escalatedToDepartmentId;
    }

    public void setEscalatedToDepartmentId(UUID escalatedToDepartmentId) {
        this.escalatedToDepartmentId = escalatedToDepartmentId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Instant getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(Instant escalatedAt) {
        this.escalatedAt = escalatedAt;
    }
}
