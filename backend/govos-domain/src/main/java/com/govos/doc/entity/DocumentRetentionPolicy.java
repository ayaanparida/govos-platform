package com.govos.doc.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.doc.enums.RetentionAction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Aggregate root for document retention and purge policy definitions (DOC-002).
 */
@Entity
@Table(
        name = "doc_document_retention_policy",
        schema = "govos",
        indexes = {
                @Index(name = "idx_doc_retention_policy_org_id", columnList = "organization_id"),
                @Index(name = "idx_doc_retention_policy_legal_hold", columnList = "legal_hold")
        })
public class DocumentRetentionPolicy extends AuditableEntity {

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "organization_id")
    private UUID organizationId;

    @PositiveOrZero
    @Column(name = "retention_days", nullable = false)
    private Integer retentionDays = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "action_on_expiry", nullable = false, length = 30)
    private RetentionAction actionOnExpiry = RetentionAction.ARCHIVE;

    @Column(name = "legal_hold", nullable = false)
    private Boolean legalHold = false;

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

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public RetentionAction getActionOnExpiry() {
        return actionOnExpiry;
    }

    public void setActionOnExpiry(RetentionAction actionOnExpiry) {
        this.actionOnExpiry = actionOnExpiry;
    }

    public Boolean getLegalHold() {
        return legalHold;
    }

    public void setLegalHold(Boolean legalHold) {
        this.legalHold = legalHold;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
