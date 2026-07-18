package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.valueobject.DocumentPath;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Aggregate root for hierarchical folder organization (DOC-002).
 */
@Entity
@Table(
        name = "doc_folder",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_folder_organization_id", columnList = "organization_id"),
                @Index(name = "idx_doc_folder_parent_folder_id", columnList = "parent_folder_id"),
                @Index(name = "idx_doc_folder_created_date", columnList = "created_date")
        })
public class Folder extends AuditableEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @NotNull
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "materializedPath", column = @Column(name = "materialized_path", length = 2048)),
            @AttributeOverride(name = "depthLevel", column = @Column(name = "depth_level"))
    })
    private DocumentPath pathMetadata;

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

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public DocumentPath getPathMetadata() {
        return pathMetadata;
    }

    public void setPathMetadata(DocumentPath pathMetadata) {
        this.pathMetadata = pathMetadata;
    }
}
