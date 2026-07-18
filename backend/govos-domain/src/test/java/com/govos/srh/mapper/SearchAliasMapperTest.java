package com.govos.srh.mapper;

import com.govos.srh.dto.SearchAliasCreateRequest;
import com.govos.srh.dto.SearchAliasDto;
import com.govos.srh.dto.SearchAliasUpdateRequest;
import com.govos.srh.entity.SearchAlias;
import com.govos.srh.entity.SearchIndex;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SearchAliasMapperTest {

    private SearchAliasMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchAliasMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoIncludingSearchIndexId() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        SearchAlias entity = SrhTestFixtures.searchAlias(SrhTestFixtures.ALIAS_ID, index);

        SearchAliasDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(SrhTestFixtures.ALIAS_ID);
        assertThat(dto.searchIndexId()).isEqualTo(SrhTestFixtures.INDEX_ID);
        assertThat(dto.aliasName()).isEqualTo(SrhTestFixtures.ALIAS_NAME);
        assertThat(dto.activeAlias()).isFalse();
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringRelationshipsAndRuntimeFields() {
        SearchAliasCreateRequest request = SrhTestFixtures.aliasCreateRequest();

        SearchAlias entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getSearchIndex()).isNull();
        assertThat(entity.getSwitchedAt()).isNull();
        assertThat(entity.getAliasName()).isEqualTo(SrhTestFixtures.ALIAS_NAME);
        assertThat(entity.getPhysicalIndexName()).isEqualTo("cmp_complaint_v1");
    }

    @Test
    void shouldUpdateEntityFromRequestWithoutTouchingIgnoredFields() {
        SearchIndex index = SrhTestFixtures.searchIndex(SrhTestFixtures.INDEX_ID);
        SearchAlias entity = SrhTestFixtures.searchAlias(UUID.randomUUID(), index);
        entity.setSwitchedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setCreatedBy("creator");
        SearchAliasUpdateRequest request = SrhTestFixtures.aliasUpdateRequest();

        mapper.updateEntity(request, entity);

        assertThat(entity.getSearchIndex()).isEqualTo(index);
        assertThat(entity.getSwitchedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(entity.getCreatedBy()).isEqualTo("creator");
        assertThat(entity.getPhysicalIndexName()).isEqualTo("cmp_complaint_v2");
        assertThat(entity.getActiveAlias()).isTrue();
    }
}
