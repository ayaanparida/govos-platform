package com.govos.srh.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.engine.EngineAdvancedSearchRequest;
import com.govos.srh.engine.EngineAdvancedSearchResult;
import com.govos.srh.engine.EngineFacetBucket;
import com.govos.srh.engine.EngineFacetResult;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.service.SearchQueryHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacetSearchTest {

    @Mock
    private SearchEngineProvider searchEngineProvider;

    @Mock
    private SearchIndexReadTargetResolver readTargetResolver;

    @Mock
    private SearchQueryHistoryService searchQueryHistoryService;

    private SearchQueryService searchQueryService;

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        searchQueryService = new SearchQueryServiceImpl(
                searchEngineProvider,
                readTargetResolver,
                searchQueryHistoryService,
                new SearchQueryValidator(properties),
                properties,
                new ObjectMapper());
        when(readTargetResolver.resolveReadTarget("CMP_COMPLAINT")).thenReturn("cmp-complaint-read");
        when(searchQueryHistoryService.record(any(SearchQueryHistoryDto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldReturnFacetAggregationsWithoutHitPayload() {
        when(searchEngineProvider.advancedSearch(any(EngineAdvancedSearchRequest.class)))
                .thenReturn(new EngineAdvancedSearchResult(
                        5,
                        List.of(),
                        List.of(
                                new EngineFacetResult("status", List.of(new EngineFacetBucket("OPEN", 3))),
                                new EngineFacetResult("priority", List.of(new EngineFacetBucket("HIGH", 2))))));

        SearchResponse response = searchQueryService.facetSearch(new FacetSearchRequest(
                "CMP_COMPLAINT",
                ORG_ID,
                null,
                "water",
                null,
                List.of("status", "priority", "category")));

        assertThat(response.results()).isEmpty();
        assertThat(response.facets()).hasSize(2);
        assertThat(response.facets().getFirst().name()).isEqualTo("status");

        ArgumentCaptor<EngineAdvancedSearchRequest> captor = ArgumentCaptor.forClass(EngineAdvancedSearchRequest.class);
        verify(searchEngineProvider).advancedSearch(captor.capture());
        assertThat(captor.getValue().facetFields()).containsExactly("status", "priority", "category");
        assertThat(captor.getValue().size()).isZero();

        ArgumentCaptor<SearchQueryHistoryDto> historyCaptor = ArgumentCaptor.forClass(SearchQueryHistoryDto.class);
        verify(searchQueryHistoryService).record(historyCaptor.capture());
        assertThat(historyCaptor.getValue().queryType()).isEqualTo(SearchQueryType.FACET);
    }
}
