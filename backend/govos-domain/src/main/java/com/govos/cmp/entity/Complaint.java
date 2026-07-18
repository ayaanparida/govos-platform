package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.valueobject.ComplaintLocation;
import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root for the complaint bounded context (CMP-001.6).
 */
@Entity
@Table(name = "cmp_complaint", schema = "govos")
public class Complaint extends AuditableEntity {

    @NotBlank
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ComplaintStatus status = ComplaintStatus.DRAFT;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private ComplaintSource source;

    @Size(max = 100)
    @Column(name = "channel", length = 100)
    private String channel;

    @Size(max = 100)
    @Column(name = "category_key", length = 100)
    private String categoryKey;

    @Size(max = 100)
    @Column(name = "sub_category_key", length = 100)
    private String subCategoryKey;

    @Size(max = 100)
    @Column(name = "complaint_type_key", length = 100)
    private String complaintTypeKey;

    @Column(name = "citizen_user_id")
    private UUID citizenUserId;

    @Column(name = "submitted_by_user_id")
    private UUID submittedByUserId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "office_id")
    private UUID officeId;

    @Column(name = "assigned_officer_id")
    private UUID assignedOfficerId;

    @Column(name = "resolved_by_user_id")
    private UUID resolvedByUserId;

    @Column(name = "closed_by_user_id")
    private UUID closedByUserId;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Size(max = 100)
    @Column(name = "rejection_reason_key", length = 100)
    private String rejectionReasonKey;

    @Size(max = 100)
    @Column(name = "closure_reason_key", length = 100)
    private String closureReasonKey;

    @Column(name = "is_duplicate", nullable = false)
    private Boolean isDuplicate = false;

    @Column(name = "primary_complaint_id")
    private UUID primaryComplaintId;

    @Column(name = "merged_into_complaint_id")
    private UUID mergedIntoComplaintId;

    @Embedded
    private ComplaintLocation location;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintAssignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintStatusHistory> statusHistory = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintEscalation> escalations = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintDuplicate> duplicates = new ArrayList<>();

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ComplaintMerge> merges = new ArrayList<>();

    @OneToOne(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ComplaintFeedback feedback;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public ComplaintPriority getPriority() {
        return priority;
    }

    public void setPriority(ComplaintPriority priority) {
        this.priority = priority;
    }

    public ComplaintSource getSource() {
        return source;
    }

    public void setSource(ComplaintSource source) {
        this.source = source;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCategoryKey() {
        return categoryKey;
    }

    public void setCategoryKey(String categoryKey) {
        this.categoryKey = categoryKey;
    }

    public String getSubCategoryKey() {
        return subCategoryKey;
    }

    public void setSubCategoryKey(String subCategoryKey) {
        this.subCategoryKey = subCategoryKey;
    }

    public String getComplaintTypeKey() {
        return complaintTypeKey;
    }

    public void setComplaintTypeKey(String complaintTypeKey) {
        this.complaintTypeKey = complaintTypeKey;
    }

    public UUID getCitizenUserId() {
        return citizenUserId;
    }

    public void setCitizenUserId(UUID citizenUserId) {
        this.citizenUserId = citizenUserId;
    }

    public UUID getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(UUID submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
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

    public UUID getAssignedOfficerId() {
        return assignedOfficerId;
    }

    public void setAssignedOfficerId(UUID assignedOfficerId) {
        this.assignedOfficerId = assignedOfficerId;
    }

    public UUID getResolvedByUserId() {
        return resolvedByUserId;
    }

    public void setResolvedByUserId(UUID resolvedByUserId) {
        this.resolvedByUserId = resolvedByUserId;
    }

    public UUID getClosedByUserId() {
        return closedByUserId;
    }

    public void setClosedByUserId(UUID closedByUserId) {
        this.closedByUserId = closedByUserId;
    }

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(UUID workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public String getRejectionReasonKey() {
        return rejectionReasonKey;
    }

    public void setRejectionReasonKey(String rejectionReasonKey) {
        this.rejectionReasonKey = rejectionReasonKey;
    }

    public String getClosureReasonKey() {
        return closureReasonKey;
    }

    public void setClosureReasonKey(String closureReasonKey) {
        this.closureReasonKey = closureReasonKey;
    }

    public Boolean getIsDuplicate() {
        return isDuplicate;
    }

    public void setIsDuplicate(Boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public UUID getPrimaryComplaintId() {
        return primaryComplaintId;
    }

    public void setPrimaryComplaintId(UUID primaryComplaintId) {
        this.primaryComplaintId = primaryComplaintId;
    }

    public UUID getMergedIntoComplaintId() {
        return mergedIntoComplaintId;
    }

    public void setMergedIntoComplaintId(UUID mergedIntoComplaintId) {
        this.mergedIntoComplaintId = mergedIntoComplaintId;
    }

    public ComplaintLocation getLocation() {
        return location;
    }

    public void setLocation(ComplaintLocation location) {
        this.location = location;
    }

    public List<ComplaintAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<ComplaintAssignment> assignments) {
        this.assignments = assignments;
    }

    public List<ComplaintComment> getComments() {
        return comments;
    }

    public void setComments(List<ComplaintComment> comments) {
        this.comments = comments;
    }

    public List<ComplaintAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ComplaintAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<ComplaintStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<ComplaintStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }

    public List<ComplaintEscalation> getEscalations() {
        return escalations;
    }

    public void setEscalations(List<ComplaintEscalation> escalations) {
        this.escalations = escalations;
    }

    public List<ComplaintDuplicate> getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(List<ComplaintDuplicate> duplicates) {
        this.duplicates = duplicates;
    }

    public List<ComplaintMerge> getMerges() {
        return merges;
    }

    public void setMerges(List<ComplaintMerge> merges) {
        this.merges = merges;
    }

    public ComplaintFeedback getFeedback() {
        return feedback;
    }

    public void setFeedback(ComplaintFeedback feedback) {
        this.feedback = feedback;
    }
}
