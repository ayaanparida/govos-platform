package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintDuplicateDetectedBy;
import com.govos.cmp.enums.ComplaintDuplicateStatus;
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

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Duplicate link metadata owned by the primary complaint aggregate (CMP-001.6).
 * Cross-complaint reference uses UUID only — no JPA association to a second aggregate root.
 */
@Entity
@Table(name = "cmp_complaint_duplicate", schema = "govos")
public class ComplaintDuplicate extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "primary_complaint_id", nullable = false)
    private Complaint complaint;

    @NotNull
    @Column(name = "duplicate_complaint_id", nullable = false)
    private UUID duplicateComplaintId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "detected_by", nullable = false, length = 20)
    private ComplaintDuplicateDetectedBy detectedBy;

    @Column(name = "detected_by_user_id")
    private UUID detectedByUserId;

    @Column(name = "similarity_score", precision = 5, scale = 4)
    private BigDecimal similarityScore;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ComplaintDuplicateStatus status = ComplaintDuplicateStatus.ACTIVE;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public UUID getDuplicateComplaintId() {
        return duplicateComplaintId;
    }

    public void setDuplicateComplaintId(UUID duplicateComplaintId) {
        this.duplicateComplaintId = duplicateComplaintId;
    }

    public ComplaintDuplicateDetectedBy getDetectedBy() {
        return detectedBy;
    }

    public void setDetectedBy(ComplaintDuplicateDetectedBy detectedBy) {
        this.detectedBy = detectedBy;
    }

    public UUID getDetectedByUserId() {
        return detectedByUserId;
    }

    public void setDetectedByUserId(UUID detectedByUserId) {
        this.detectedByUserId = detectedByUserId;
    }

    public BigDecimal getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(BigDecimal similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public ComplaintDuplicateStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintDuplicateStatus status) {
        this.status = status;
    }
}
