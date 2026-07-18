package com.govos.srh.mapper;

import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.support.SrhTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchQueryHistoryMapperTest {

    private SearchQueryHistoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SearchQueryHistoryMapperImpl();
    }

    @Test
    void shouldMapEntityToDto() {
        SearchQueryHistory entity = SrhTestFixtures.searchQueryHistory(SrhTestFixtures.HISTORY_ID);

        SearchQueryHistoryDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(SrhTestFixtures.HISTORY_ID);
        assertThat(dto.organizationId()).isEqualTo(SrhTestFixtures.ORG_ID);
        assertThat(dto.userId()).isEqualTo(SrhTestFixtures.USER_ID);
        assertThat(dto.queryType()).isEqualTo(SearchQueryType.SEARCH);
        assertThat(dto.queryText()).isEqualTo("water leak");
        assertThat(dto.executionTimeMs()).isEqualTo(120L);
        assertThat(dto.resultCount()).isEqualTo(5L);
    }
}
