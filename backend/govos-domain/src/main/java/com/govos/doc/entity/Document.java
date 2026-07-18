package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Primary aggregate root for document identity and lifecycle (DOC-002).
 */
@Entity
@Table(
        name = "doc_document",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_document_organization_id", columnList = "organization_id"),
                @Index(name = "idx_doc_document_status", columnList = "status"),
                @Index(name = "idx_doc_document_category_id", columnList = "category_id"),
                @Index(name = "idx_doc_document_folder_id", columnList = "folder_id"),
                @Index(name = "idx_doc_document_created_date", columnList = "created_date"),
                @Index(name = "idx_doc_document_reference_id", columnList = "reference_id")
        })
public class Document extends AuditableEntity {

    @NotBlank
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DocumentStatus status = DocumentStatus.UPLOADED;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "classification", nullable = false, length = 30)
    private DocumentClassification classification = DocumentClassification.INTERNAL;

    @Size(max = 255)
    @Column(name = "mime_type", length = 255)
    private String mimeType;

    @Size(max = 50)
    @Column(name = "module_code", length = 50)
    private String moduleCode;

    @Size(max = 100)
    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Size(max = 100)
    @Column(name = "document_number", length = 100)
    private String documentNumber;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "active_version_id")
    private DocumentVersion activeVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private DocumentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retention_policy_id")
    private DocumentRetentionPolicy retentionPolicy;

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    private List<DocumentVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    private List<DocumentMetadata> metadataEntries = new ArrayList<>();

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    private List<DocumentShare> shares = new ArrayList<>();

    @OneToMany(mappedBy = "document", fetch = FetchType.LAZY)
    private List<DocumentAccessLog> accessLogs = new ArrayList<>();

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

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public DocumentClassification getClassification() {
        return classification;
    }

    public void setClassification(DocumentClassification classification) {
        this.classification = classification;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public DocumentVersion getActiveVersion() {
        return activeVersion;
    }

    public void setActiveVersion(DocumentVersion activeVersion) {
        this.activeVersion = activeVersion;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public DocumentCategory getCategory() {
        return category;
    }

    public void setCategory(DocumentCategory category) {
        this.category = category;
    }

    public DocumentRetentionPolicy getRetentionPolicy() {
        return retentionPolicy;
    }

    public void setRetentionPolicy(DocumentRetentionPolicy retentionPolicy) {
        this.retentionPolicy = retentionPolicy;
    }

    public List<DocumentVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<DocumentVersion> versions) {
        this.versions = versions;
    }

    public List<DocumentMetadata> getMetadataEntries() {
        return metadataEntries;
    }

    public void setMetadataEntries(List<DocumentMetadata> metadataEntries) {
        this.metadataEntries = metadataEntries;
    }

    public List<DocumentShare> getShares() {
        return shares;
    }

    public void setShares(List<DocumentShare> shares) {
        this.shares = shares;
    }

    public List<DocumentAccessLog> getAccessLogs() {
        return accessLogs;
    }

    public void setAccessLogs(List<DocumentAccessLog> accessLogs) {
        this.accessLogs = accessLogs;
    }
}
