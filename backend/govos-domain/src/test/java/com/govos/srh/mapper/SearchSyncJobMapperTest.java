package com.govos.srh.mapper;

import com.govos.srh.dto.SearchSyncJobCreateRequest;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.dto.SearchSyncJobUpdateRequest;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchSyncJobMapperTest {

    private SearchSyncJobMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchSyncJobMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoIncludingStatusRenameAndSearchIndexId() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        SearchSyncJob entity = SrhTestFixtures.searchSyncJob(SrhTestFixtures.JOB_ID, index);
        entity.setStatus(SearchJobStatus.RUNNING);

        SearchSyncJobDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(SrhTestFixtures.JOB_ID);
        assertThat(dto.searchIndexId()).isEqualTo(SrhTestFixtures.INDEX_ID);
        assertThat(dto.jobStatus()).isEqualTo(SearchJobStatus.RUNNING);
        assertThat(dto.jobType()).isEqualTo(SearchJobType.FULL_REINDEX);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringLifecycleFields() {
        SearchSyncJobCreateRequest request = SrhTestFixtures.syncJobCreateRequest();

        SearchSyncJob entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getSearchIndex()).isNull();
        assertThat(entity.getStatus()).isEqualTo(SearchJobStatus.PENDING);
        assertThat(entity.getStartedAt()).isNull();
        assertThat(entity.getProcessedCount()).isEqualTo(0L);
        assertThat(entity.getJobName()).isEqualTo("Full reindex");
        assertThat(entity.getJobType()).isEqualTo(SearchJobType.FULL_REINDEX);
    }

    @Test
    void shouldUpdateEntityFromRequestWithoutTouchingIgnoredFields() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        SearchSyncJob entity = SrhTestFixtures.searchSyncJob(UUID.randomUUID(), index);
        entity.setStatus(SearchJobStatus.RUNNING);
        entity.setProcessedCount(10L);
        SearchSyncJobUpdateRequest request = SrhTestFixtures.syncJobUpdateRequest();

        mapper.updateEntity(request, entity);

        assertThat(entity.getSearchIndex()).isEqualTo(index);
        assertThat(entity.getStatus()).isEqualTo(SearchJobStatus.RUNNING);
        assertThat(entity.getProcessedCount()).isEqualTo(10L);
        assertThat(entity.getJobType()).isEqualTo(SearchJobType.INCREMENTAL_REINDEX);
        assertThat(entity.getErrorMessage()).isEqualTo("No errors");
    }
}
