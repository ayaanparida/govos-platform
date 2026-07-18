package com.govos.srh.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.engine.EngineAdvancedSearchRequest;
import com.govos.srh.engine.EngineAdvancedSearchResult;
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
class GeoSearchTest {

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
    void shouldExecuteRadiusGeoSearchWithDistanceSorting() {
        when(searchEngineProvider.advancedSearch(any(EngineAdvancedSearchRequest.class)))
                .thenReturn(new EngineAdvancedSearchResult(
                        1,
                        List.of(new EngineSearchHit("doc-1", 1.0, Map.of("title", "Nearby"), null)),
                        List.of()));

        SearchResponse response = searchQueryService.geoSearch(new GeoSearchRequest(
                "CMP_COMPLAINT",
                ORG_ID,
                null,
                new BigDecimal("12.9716000"),
                new BigDecimal("77.5946000"),
                5.0,
                null,
                null,
                null,
                null,
                "leak",
                null,
                new SearchPage(0, 20),
                true));

        assertThat(response.totalHits()).isEqualTo(1);

        ArgumentCaptor<EngineAdvancedSearchRequest> captor = ArgumentCaptor.forClass(EngineAdvancedSearchRequest.class);
        verify(searchEngineProvider).advancedSearch(captor.capture());
        assertThat(captor.getValue().radiusKm()).isEqualTo(5.0);
        assertThat(captor.getValue().sortByDistance()).isTrue();
        assertThat(captor.getValue().organizationId()).isEqualTo(ORG_ID);

        ArgumentCaptor<SearchQueryHistoryDto> historyCaptor = ArgumentCaptor.forClass(SearchQueryHistoryDto.class);
        verify(searchQueryHistoryService).record(historyCaptor.capture());
        assertThat(historyCaptor.getValue().queryType()).isEqualTo(SearchQueryType.GEO);
    }

    @Test
    void shouldExecuteBoundingBoxGeoSearch() {
        when(searchEngineProvider.advancedSearch(any(EngineAdvancedSearchRequest.class)))
                .thenReturn(new EngineAdvancedSearchResult(0, List.of(), List.of()));

        searchQueryService.geoSearch(new GeoSearchRequest(
                "CMP_COMPLAINT",
                ORG_ID,
                null,
                new BigDecimal("12.9716000"),
                new BigDecimal("77.5946000"),
                null,
                new BigDecimal("13.0000000"),
                new BigDecimal("77.5000000"),
                new BigDecimal("12.9000000"),
                new BigDecimal("77.7000000"),
                null,
                null,
                null,
                false));

        ArgumentCaptor<EngineAdvancedSearchRequest> captor = ArgumentCaptor.forClass(EngineAdvancedSearchRequest.class);
        verify(searchEngineProvider).advancedSearch(captor.capture());
        assertThat(captor.getValue().topLeftLatitude()).isEqualTo(13.0);
        assertThat(captor.getValue().bottomRightLongitude()).isEqualTo(77.7);
    }

    @Test
    void shouldRequireRadiusOrBoundingBox() {
        assertThatThrownBy(() -> searchQueryService.geoSearch(new GeoSearchRequest(
                "CMP_COMPLAINT",
                ORG_ID,
                null,
                new BigDecimal("12.9716000"),
                new BigDecimal("77.5946000"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false)))
                .isInstanceOf(SearchQueryException.class)
                .hasMessageContaining("radiusKm or bounding box");
    }
}
