package com.govos.cmp.entity;

import com.govos.cmp.enums.ComplaintAttachmentType;
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

import java.util.UUID;

@Entity
@Table(name = "cmp_complaint_attachment", schema = "govos")
public class ComplaintAttachment extends AuditableEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @NotNull
    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "document_version_id")
    private UUID documentVersionId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 20)
    private ComplaintAttachmentType attachmentType;

    @Size(max = 255)
    @Column(name = "display_name", length = 255)
    private String displayName;

    @NotNull
    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public void setDocumentId(UUID documentId) {
        this.documentId = documentId;
    }

    public UUID getDocumentVersionId() {
        return documentVersionId;
    }

    public void setDocumentVersionId(UUID documentVersionId) {
        this.documentVersionId = documentVersionId;
    }

    public ComplaintAttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(ComplaintAttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(UUID uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
