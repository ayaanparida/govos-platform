package com.govos.srh.entity;

import com.govos.common.entity.AuditableEntity;
import com.govos.srh.enums.SearchQueryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregate root for search query history (SRH-001).
 */
@Entity
@Table(name = "srh_search_query_history", schema = "govos")
public class SearchQueryHistory extends AuditableEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "query_text", nullable = false, length = 2000)
    private String queryText;

    @Enumerated(EnumType.STRING)
    @Column(name = "query_type", nullable = false, length = 30)
    private SearchQueryType queryType = SearchQueryType.SEARCH;

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "result_count", nullable = false)
    private Long resultCount = 0L;

    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs = 0L;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public SearchQueryType getQueryType() {
        return queryType;
    }

    public void setQueryType(SearchQueryType queryType) {
        this.queryType = queryType;
    }

    public String getFiltersJson() {
        return filtersJson;
    }

    public void setFiltersJson(String filtersJson) {
        this.filtersJson = filtersJson;
    }

    public Long getResultCount() {
        return resultCount;
    }

    public void setResultCount(Long resultCount) {
        this.resultCount = resultCount;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public Instant getSearchedAt() {
        return searchedAt;
    }

    public void setSearchedAt(Instant searchedAt) {
        this.searchedAt = searchedAt;
    }
}
