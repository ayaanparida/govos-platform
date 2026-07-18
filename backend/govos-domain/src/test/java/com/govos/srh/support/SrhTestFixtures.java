package com.govos.srh.support;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchDocument;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.valueobject.SearchDocumentMetadata;

import java.time.Instant;
import java.util.UUID;

public final class SrhTestFixtures {

    public static final UUID INDEX_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID DOCUMENT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    public static final UUID ALIAS_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    public static final UUID JOB_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    public static final UUID HISTORY_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
    public static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID REFERENCE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final String INDEX_CODE = "CMP_COMPLAINT";
    public static final String ENTITY_TYPE = "COMPLAINT";
    public static final String ALIAS_NAME = "cmp-complaint-read";

    private SrhTestFixtures() {
    }

    public static SearchIndex searchIndex(UUID id) {
        SearchIndex index = new SearchIndex();
        index.setId(id);
        index.setCode(INDEX_CODE);
        index.setName("Complaint Search Index");
        index.setDescription("CMP complaint index");
        index.setEngineType(SearchEngineType.OPENSEARCH);
        index.setStatus(SearchIndexStatus.ACTIVE);
        index.setMappingVersion(1);
        index.setPhysicalIndexName("cmp_complaint_v1");
        index.setAliasName(ALIAS_NAME);
        index.setActiveDocumentCount(0L);
        index.setActive(true);
        index.setDeleted(false);
        index.setVersion(0L);
        index.setCreatedBy("system");
        index.setCreatedDate(Instant.parse("2026-01-01T00:00:00Z"));
        index.setUpdatedBy("system");
        index.setUpdatedDate(Instant.parse("2026-01-01T00:00:00Z"));
        return index;
    }

    public static SearchDocument searchDocument(UUID id, SearchIndex searchIndex) {
        SearchDocument document = new SearchDocument();
        document.setId(id);
        document.setCode("DOC-001");
        document.setSearchDocumentId(UUID.randomUUID());
        document.setEntityType(ENTITY_TYPE);
        document.setReferenceId(REFERENCE_ID);
        document.setReferenceCode("CMP-2026-0001");
        document.setOrganizationId(ORG_ID);
        document.setDocumentJson("{\"title\":\"Leak\"}");
        document.setSearchText("water leak");
        document.setStatus(SearchDocumentStatus.NOT_INDEXED);
        document.setSearchVersion(0L);
        document.setSearchIndex(searchIndex);
        document.setMetadata(documentMetadata());
        document.setActive(true);
        document.setDeleted(false);
        document.setVersion(0L);
        return document;
    }

    public static SearchDocumentMetadata documentMetadata() {
        SearchDocumentMetadata metadata = new SearchDocumentMetadata();
        metadata.setOrganizationId(ORG_ID);
        metadata.setEntityType(ENTITY_TYPE);
        metadata.setReferenceId(REFERENCE_ID);
        metadata.setReferenceCode("CMP-2026-0001");
        metadata.setMappingVersion(1);
        metadata.setIndexedAt(Instant.parse("2026-01-02T00:00:00Z"));
        metadata.setLastIndexedAt(Instant.parse("2026-01-02T01:00:00Z"));
        return metadata;
    }

    public static SearchAlias searchAlias(UUID id, SearchIndex searchIndex) {
        SearchAlias alias = new SearchAlias();
        alias.setId(id);
        alias.setCode("ALIAS-001");
        alias.setAliasName(ALIAS_NAME);
        alias.setPhysicalIndexName("cmp_complaint_v1");
        alias.setActiveAlias(false);
        alias.setSearchIndex(searchIndex);
        alias.setActive(true);
        alias.setDeleted(false);
        alias.setVersion(0L);
        return alias;
    }

    public static SearchSyncJob searchSyncJob(UUID id, SearchIndex searchIndex) {
        SearchSyncJob job = new SearchSyncJob();
        job.setId(id);
        job.setCode("JOB-001");
        job.setJobName("Full reindex");
        job.setJobType(SearchJobType.FULL_REINDEX);
        job.setStatus(SearchJobStatus.PENDING);
        job.setProcessedCount(0L);
        job.setSuccessCount(0L);
        job.setFailureCount(0L);
        job.setSearchIndex(searchIndex);
        job.setActive(true);
        job.setDeleted(false);
        job.setVersion(0L);
        return job;
    }

    public static SearchQueryHistory searchQueryHistory(UUID id) {
        SearchQueryHistory history = new SearchQueryHistory();
        history.setId(id);
        history.setCode("QH-001");
        history.setOrganizationId(ORG_ID);
        history.setUserId(USER_ID);
        history.setQueryText("water leak");
        history.setQueryType(SearchQueryType.SEARCH);
        history.setFiltersJson("{}");
        history.setResultCount(5L);
        history.setExecutionTimeMs(120L);
        history.setSearchedAt(Instant.parse("2026-01-03T10:00:00Z"));
        history.setActive(true);
        history.setDeleted(false);
        history.setVersion(0L);
        return history;
    }

    public static SearchIndexCreateRequest indexCreateRequest() {
        return new SearchIndexCreateRequest(
                INDEX_CODE,
                "Complaint Search Index",
                "CMP complaint index",
                SearchEngineType.OPENSEARCH,
                1,
                ALIAS_NAME,
                true);
    }

    public static SearchIndexUpdateRequest indexUpdateRequest() {
        return new SearchIndexUpdateRequest(
                INDEX_CODE,
                "Complaint Search Index Updated",
                "Updated description",
                SearchEngineType.OPENSEARCH,
                2,
                ALIAS_NAME,
                true,
                0L);
    }

    public static SearchDocumentCreateRequest documentCreateRequest() {
        return new SearchDocumentCreateRequest(
                "DOC-001",
                INDEX_ID,
                UUID.randomUUID(),
                ENTITY_TYPE,
                REFERENCE_ID,
                "CMP-2026-0001",
                ORG_ID,
                "{\"title\":\"Leak\"}",
                "water leak",
                0L,
                null,
                null,
                ORG_ID,
                ENTITY_TYPE,
                REFERENCE_ID,
                "CMP-2026-0001",
                1,
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2026-01-02T01:00:00Z"),
                true);
    }

    public static SearchDocumentUpdateRequest documentUpdateRequest() {
        return new SearchDocumentUpdateRequest(
                "DOC-001",
                ENTITY_TYPE,
                REFERENCE_ID,
                "CMP-2026-0001",
                ORG_ID,
                "{\"title\":\"Updated\"}",
                "updated leak",
                1L,
                null,
                null,
                ORG_ID,
                ENTITY_TYPE,
                REFERENCE_ID,
                "CMP-2026-0001",
                1,
                null,
                null,
                true,
                0L);
    }

    public static SearchAliasCreateRequest aliasCreateRequest() {
        return new SearchAliasCreateRequest(
                "ALIAS-001",
                INDEX_ID,
                ALIAS_NAME,
                "cmp_complaint_v1",
                false,
                true);
    }

    public static SearchAliasUpdateRequest aliasUpdateRequest() {
        return new SearchAliasUpdateRequest(
                "ALIAS-001",
                ALIAS_NAME,
                "cmp_complaint_v2",
                true,
                true,
                0L);
    }

    public static SearchSyncJobCreateRequest syncJobCreateRequest() {
        return new SearchSyncJobCreateRequest(
                "JOB-001",
                INDEX_ID,
                "Full reindex",
                SearchJobType.FULL_REINDEX,
                true);
    }

    public static SearchSyncJobUpdateRequest syncJobUpdateRequest() {
        return new SearchSyncJobUpdateRequest(
                "JOB-001",
                "Full reindex updated",
                SearchJobType.INCREMENTAL_REINDEX,
                "No errors",
                true,
                0L);
    }

    public static SearchQueryHistoryDto queryHistoryDto() {
        return new SearchQueryHistoryDto(
                null,
                "QH-001",
                ORG_ID,
                USER_ID,
                "water leak",
                SearchQueryType.SEARCH,
                "{}",
                5L,
                120L,
                Instant.parse("2026-01-03T10:00:00Z"),
                true,
                null,
                null,
                null,
                null,
                null);
    }
}
