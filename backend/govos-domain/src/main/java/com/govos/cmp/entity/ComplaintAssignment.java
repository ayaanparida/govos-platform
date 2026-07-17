package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintAssignmentStatus;
import com.govos.cmp.enums.ComplaintAssignmentType;
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
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cmp_complaint_assignment", schema = "govos")
public class ComplaintAssignment extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 20)
    private ComplaintAssignmentType assignmentType;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "office_id")
    private UUID officeId;

    @Column(name = "officer_user_id")
    private UUID officerUserId;

    @Column(name = "assigned_by_user_id")
    private UUID assignedByUserId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_status", nullable = false, length = 20)
    private ComplaintAssignmentStatus assignmentStatus = ComplaintAssignmentStatus.PENDING;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Size(max = 100)
    @Column(name = "rejection_reason_key", length = 100)
    private String rejectionReasonKey;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @NotNull
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public ComplaintAssignmentType getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(ComplaintAssignmentType assignmentType) {
        this.assignmentType = assignmentType;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(UUID departmentId) {
        this.departmentId = departmentId;
    }

    public UUID getOfficeId() {
        return officeId;
    }

    public void setOfficeId(UUID officeId) {
        this.officeId = officeId;
    }

    public UUID getOfficerUserId() {
        return officerUserId;
    }

    public void setOfficerUserId(UUID officerUserId) {
        this.officerUserId = officerUserId;
    }

    public UUID getAssignedByUserId() {
        return assignedByUserId;
    }

    public void setAssignedByUserId(UUID assignedByUserId) {
        this.assignedByUserId = assignedByUserId;
    }

    public ComplaintAssignmentStatus getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(ComplaintAssignmentStatus assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(Instant acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public Instant getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(Instant rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReasonKey() {
        return rejectionReasonKey;
    }

    public void setRejectionReasonKey(String rejectionReasonKey) {
        this.rejectionReasonKey = rejectionReasonKey;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }
}
