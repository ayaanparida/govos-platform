package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Aggregate root for document taxonomy and classification (DOC-002).
 */
@Entity
@Table(
        name = "doc_document_category",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_document_category_org_id", columnList = "organization_id"),
                @Index(name = "idx_doc_document_category_parent_id", columnList = "parent_category_id")
        })
public class DocumentCategory extends AuditableEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "organization_id")
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private DocumentCategory parentCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_retention_policy_id")
    private DocumentRetentionPolicy defaultRetentionPolicy;

    @Column(name = "allowed_mime_types", columnDefinition = "TEXT")
    private String allowedMimeTypes;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public DocumentCategory getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(DocumentCategory parentCategory) {
        this.parentCategory = parentCategory;
    }

    public DocumentRetentionPolicy getDefaultRetentionPolicy() {
        return defaultRetentionPolicy;
    }

    public void setDefaultRetentionPolicy(DocumentRetentionPolicy defaultRetentionPolicy) {
        this.defaultRetentionPolicy = defaultRetentionPolicy;
    }

    public String getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(String allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
