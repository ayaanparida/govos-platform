package com.govos.srh.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.valueobject.SearchDocumentMetadata;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for search document synchronization metadata (SRH-001).
 */
@Entity
@Table(name = "srh_search_document", schema = "govos")
public class SearchDocument extends AuditableEntity {

    @Column(name = "search_document_id", nullable = false)
    private UUID searchDocumentId;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "reference_id", nullable = false)
    private UUID referenceId;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "document_json", columnDefinition = "TEXT")
    private String documentJson;

    @Column(name = "search_text", columnDefinition = "TEXT")
    private String searchText;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false, length = 30)
    private SearchDocumentStatus status = SearchDocumentStatus.NOT_INDEXED;

    @Column(name = "search_version", nullable = false)
    private Long searchVersion = 0L;

    @Column(name = "indexed_at")
    private Instant indexedAt;

    @Column(name = "last_indexed_at")
    private Instant lastIndexedAt;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "organizationId", column = @Column(name = "md_organization_id")),
            @AttributeOverride(name = "entityType", column = @Column(name = "md_entity_type")),
            @AttributeOverride(name = "referenceId", column = @Column(name = "md_reference_id")),
            @AttributeOverride(name = "referenceCode", column = @Column(name = "md_reference_code")),
            @AttributeOverride(name = "mappingVersion", column = @Column(name = "md_mapping_version")),
            @AttributeOverride(name = "indexedAt", column = @Column(name = "md_indexed_at")),
            @AttributeOverride(name = "lastIndexedAt", column = @Column(name = "md_last_indexed_at"))
    })
    private SearchDocumentMetadata metadata;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_index_id", nullable = false)
    private SearchIndex searchIndex;

    public UUID getSearchDocumentId() {
        return searchDocumentId;
    }

    public void setSearchDocumentId(UUID searchDocumentId) {
        this.searchDocumentId = searchDocumentId;
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

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getDocumentJson() {
        return documentJson;
    }

    public void setDocumentJson(String documentJson) {
        this.documentJson = documentJson;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public SearchDocumentStatus getStatus() {
        return status;
    }

    public void setStatus(SearchDocumentStatus status) {
        this.status = status;
    }

    public Long getSearchVersion() {
        return searchVersion;
    }

    public void setSearchVersion(Long searchVersion) {
        this.searchVersion = searchVersion;
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

    public SearchDocumentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SearchDocumentMetadata metadata) {
        this.metadata = metadata;
    }

    public SearchIndex getSearchIndex() {
        return searchIndex;
    }

    public void setSearchIndex(SearchIndex searchIndex) {
        this.searchIndex = searchIndex;
    }
}
