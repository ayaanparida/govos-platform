package com.govos.srh.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root for logical search index configuration (SRH-001).
 */
@Entity
@Table(name = "srh_search_index", schema = "govos")
public class SearchIndex extends AuditableEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine_type", nullable = false, length = 30)
    private SearchEngineType engineType = SearchEngineType.OPENSEARCH;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SearchIndexStatus status = SearchIndexStatus.ACTIVE;

    @Column(name = "mapping_version", nullable = false)
    private Integer mappingVersion = 1;

    @Column(name = "physical_index_name", length = 255)
    private String physicalIndexName;

    @Column(name = "alias_name", length = 255)
    private String aliasName;

    @Column(name = "active_document_count", nullable = false)
    private Long activeDocumentCount = 0L;

    @Column(name = "last_reindexed_at")
    private Instant lastReindexedAt;

    @OneToMany(mappedBy = "searchIndex", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SearchDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "searchIndex", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SearchAlias> aliases = new ArrayList<>();

    @OneToMany(mappedBy = "searchIndex", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<SearchSyncJob> syncJobs = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SearchEngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(SearchEngineType engineType) {
        this.engineType = engineType;
    }

    public SearchIndexStatus getStatus() {
        return status;
    }

    public void setStatus(SearchIndexStatus status) {
        this.status = status;
    }

    public Integer getMappingVersion() {
        return mappingVersion;
    }

    public void setMappingVersion(Integer mappingVersion) {
        this.mappingVersion = mappingVersion;
    }

    public String getPhysicalIndexName() {
        return physicalIndexName;
    }

    public void setPhysicalIndexName(String physicalIndexName) {
        this.physicalIndexName = physicalIndexName;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Long getActiveDocumentCount() {
        return activeDocumentCount;
    }

    public void setActiveDocumentCount(Long activeDocumentCount) {
        this.activeDocumentCount = activeDocumentCount;
    }

    public Instant getLastReindexedAt() {
        return lastReindexedAt;
    }

    public void setLastReindexedAt(Instant lastReindexedAt) {
        this.lastReindexedAt = lastReindexedAt;
    }

    public List<SearchDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<SearchDocument> documents) {
        this.documents = documents;
    }

    public List<SearchAlias> getAliases() {
        return aliases;
    }

    public void setAliases(List<SearchAlias> aliases) {
        this.aliases = aliases;
    }

    public List<SearchSyncJob> getSyncJobs() {
        return syncJobs;
    }

    public void setSyncJobs(List<SearchSyncJob> syncJobs) {
        this.syncJobs = syncJobs;
    }
}
