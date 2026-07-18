package com.govos.srh.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.engine.EngineAdvancedSearchRequest;
import com.govos.srh.engine.EngineAdvancedSearchResult;
import com.govos.srh.engine.EngineAutocompleteRequest;
import com.govos.srh.engine.EngineCountRequest;
import com.govos.srh.engine.EngineFacetBucket;
import com.govos.srh.engine.EngineFacetResult;
import com.govos.srh.engine.EngineSearchHit;
import com.govos.srh.engine.SearchEngineProvider;
import com.govos.srh.enums.SearchQueryType;
import com.govos.srh.exception.SearchQueryException;
import com.govos.srh.service.SearchQueryHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SearchQueryServiceTest {

    @Mock
    private SearchEngineProvider searchEngineProvider;

    @Mock
    private SearchIndexReadTargetResolver readTargetResolver;

    @Mock
    private SearchQueryHistoryService searchQueryHistoryService;

    private SearchQueryService searchQueryService;

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String INDEX_CODE = "CMP_COMPLAINT";
    private static final String INDEX_NAME = "cmp-complaint-read";

    @BeforeEach
    void setUp() {
        SearchProperties properties = new SearchProperties();
        properties.setDefaultPageSize(20);
        properties.setMaxPageSize(100);
        properties.setQueryTimeoutMs(5000L);

        searchQueryService = new SearchQueryServiceImpl(
                searchEngineProvider,
                readTargetResolver,
                searchQueryHistoryService,
                new SearchQueryValidator(properties),
                properties,
                new ObjectMapper());

        when(readTargetResolver.resolveReadTarget(INDEX_CODE)).thenReturn(INDEX_NAME);
        when(searchQueryHistoryService.record(any(SearchQueryHistoryDto.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldExecuteSearchWithOrganizationFilterAndRecordHistory() {
        when(searchEngineProvider.advancedSearch(any(EngineAdvancedSearchRequest.class)))
                .thenReturn(sampleEngineResult());

        SearchRequest request = new SearchRequest(
                INDEX_CODE,
                ORG_ID,
                USER_ID,
                "water leak",
                SearchQueryMode.FULL_TEXT,
                new SearchFilters("COMPLAINT", "SUBMITTED", "MEDIUM", null, null,
                        null, null, null, null, true, false),
                new SearchPage(0, 20),
                List.of(new SearchSort("updatedDate", SortDirection.DESC)),
                true,
                List.of("status", "priority"));

        SearchResponse response = searchQueryService.search(request);

        assertThat(response.totalHits()).isEqualTo(1);
        assertThat(response.results()).hasSize(1);
        assertThat(response.results().getFirst().highlights()).containsKey("searchText");
        assertThat(response.facets()).hasSize(1);

        ArgumentCaptor<EngineAdvancedSearchRequest> engineCaptor =
                ArgumentCaptor.forClass(EngineAdvancedSearchRequest.class);
        verify(searchEngineProvider).advancedSearch(engineCaptor.capture());
        assertThat(engineCaptor.getValue().organizationId()).isEqualTo(ORG_ID);
        assertThat(engineCaptor.getValue().indexName()).isEqualTo(INDEX_NAME);
        assertThat(engineCaptor.getValue().highlight()).isTrue();

        ArgumentCaptor<SearchQueryHistoryDto> historyCaptor = ArgumentCaptor.forClass(SearchQueryHistoryDto.class);
        verify(searchQueryHistoryService).record(historyCaptor.capture());
        assertThat(historyCaptor.getValue().organizationId()).isEqualTo(ORG_ID);
        assertThat(historyCaptor.getValue().queryType()).isEqualTo(SearchQueryType.SEARCH);
    }

    @Test
    void shouldRejectInvalidPageSize() {
        SearchRequest request = new SearchRequest(
                INDEX_CODE, ORG_ID, USER_ID, "water", null, null,
                new SearchPage(0, 101), null, false, null);

        assertThatThrownBy(() -> searchQueryService.search(request))
                .isInstanceOf(SearchQueryException.class)
                .hasMessageContaining("page size");
    }

    @Test
    void shouldCountMatchingDocuments() {
        when(searchEngineProvider.countDocuments(any(EngineCountRequest.class))).thenReturn(42L);

        long count = searchQueryService.count(new SearchRequest(
                INDEX_CODE, ORG_ID, USER_ID, "water", SearchQueryMode.PREFIX,
                null, null, null, false, null));

        assertThat(count).isEqualTo(42L);
        verify(searchEngineProvider).countDocuments(any(EngineCountRequest.class));
    }

    private static EngineAdvancedSearchResult sampleEngineResult() {
        return new EngineAdvancedSearchResult(
                1,
                List.of(new EngineSearchHit(
                        "doc-1",
                        1.23,
                        Map.of("organizationId", ORG_ID.toString(), "title", "Water leak"),
                        Map.of("searchText", List.of("<em>water</em> leak")))),
                List.of(new EngineFacetResult(
                        "status",
                        List.of(new EngineFacetBucket("SUBMITTED", 1)))));
    }
}
