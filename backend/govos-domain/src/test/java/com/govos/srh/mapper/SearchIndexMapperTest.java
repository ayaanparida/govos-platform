package com.govos.srh.mapper;

import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.dto.SearchIndexUpdateRequest;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchIndexMapperTest {

    private SearchIndexMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchIndexMapperImpl();
    }

    @Test
    void shouldMapEntityToDto() {
        SearchIndex entity = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);

        SearchIndexDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(SrhTestFixtures.INDEX_ID);
        assertThat(dto.code()).isEqualTo(SrhTestFixtures.INDEX_CODE);
        assertThat(dto.name()).isEqualTo(entity.getName());
        assertThat(dto.engineType()).isEqualTo(SearchEngineType.OPENSEARCH);
        assertThat(dto.status()).isEqualTo(SearchIndexStatus.ACTIVE);
        assertThat(dto.createdBy()).isEqualTo("system");
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditAndRuntimeFields() {
        SearchIndexCreateRequest request = SrhTestFixtures.indexCreateRequest();

        SearchIndex entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getStatus()).isEqualTo(SearchIndexStatus.ACTIVE);
        assertThat(entity.getPhysicalIndexName()).isNull();
        assertThat(entity.getName()).isEqualTo(request.name());
        assertThat(entity.getEngineType()).isEqualTo(SearchEngineType.OPENSEARCH);
        assertThat(entity.getMappingVersion()).isEqualTo(1);
    }

    @Test
    void shouldUpdateEntityFromRequestWithoutTouchingIgnoredFields() {
        SearchIndex entity = SrhTestFixtures.searchIndex(UUID.randomUUID());
        entity.setCreatedBy("creator");
        entity.setCreatedDate(Instant.parse("2026-01-01T00:00:00Z"));
        SearchIndexUpdateRequest request = new SearchIndexUpdateRequest(
                "NEW_CODE", "Updated Name", "Updated", SearchEngineType.OPENSEARCH, 3,
                "new-alias", false, 1L);

        mapper.updateEntity(request, entity);

        assertThat(entity.getCode()).isEqualTo(SrhTestFixtures.INDEX_CODE);
        assertThat(entity.getStatus()).isEqualTo(SearchIndexStatus.ACTIVE);
        assertThat(entity.getPhysicalIndexName()).isEqualTo("cmp_complaint_v1");
        assertThat(entity.getCreatedBy()).isEqualTo("creator");
        assertThat(entity.getName()).isEqualTo("Updated Name");
        assertThat(entity.getMappingVersion()).isEqualTo(3);
    }
}
