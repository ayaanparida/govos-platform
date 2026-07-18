package com.govos.srh.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Aggregate root for search index alias routing (SRH-001).
 */
@Entity
@Table(name = "srh_search_alias", schema = "govos")
public class SearchAlias extends AuditableEntity {

    @Column(name = "alias_name", nullable = false, length = 255)
    private String aliasName;

    @Column(name = "physical_index_name", nullable = false, length = 255)
    private String physicalIndexName;

    @Column(name = "active_alias", nullable = false)
    private Boolean activeAlias = false;

    @Column(name = "switched_at")
    private Instant switchedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "search_index_id", nullable = false)
    private SearchIndex searchIndex;

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getPhysicalIndexName() {
        return physicalIndexName;
    }

    public void setPhysicalIndexName(String physicalIndexName) {
        this.physicalIndexName = physicalIndexName;
    }

    public Boolean getActiveAlias() {
        return activeAlias;
    }

    public void setActiveAlias(Boolean activeAlias) {
        this.activeAlias = activeAlias;
    }

    public Instant getSwitchedAt() {
        return switchedAt;
    }

    public void setSwitchedAt(Instant switchedAt) {
        this.switchedAt = switchedAt;
    }

    public SearchIndex getSearchIndex() {
        return searchIndex;
    }

    public void setSearchIndex(SearchIndex searchIndex) {
        this.searchIndex = searchIndex;
    }
}
