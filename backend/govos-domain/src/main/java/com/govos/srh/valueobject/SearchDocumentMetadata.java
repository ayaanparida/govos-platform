package com.govos.srh.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.UUID;

/**
 * Embedded metadata for search document synchronization (SRH-001).
 */
@Embeddable
public class SearchDocumentMetadata {

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "mapping_version")
    private Integer mappingVersion;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "last_indexed_at")
    private Instant lastIndexedAt;

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
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

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public Integer getMappingVersion() {
        return mappingVersion;
    }

    public void setMappingVersion(Integer mappingVersion) {
        this.mappingVersion = mappingVersion;
    }

    public Instant getIndexedAt() {
        return indexedAt;
    }

    public void setIndexedAt(Instant indexedAt) {
        this.indexedAt = indexedAt;
    }

    public Instant getLastIndexedAt() {
        return lastIndexedAt;
    }

    public void setLastIndexedAt(Instant lastIndexedAt) {
        this.lastIndexedAt = lastIndexedAt;
    }
}
