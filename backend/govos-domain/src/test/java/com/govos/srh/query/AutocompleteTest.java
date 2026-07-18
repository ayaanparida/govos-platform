package com.govos.srh.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.srh.config.SearchProperties;
import com.govos.srh.dto.SearchQueryHistoryDto;
import com.govos.srh.engine.EngineAutocompleteRequest;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AutocompleteTest {

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
    void shouldReturnTopSuggestionsWithinOrganization() {
        when(searchEngineProvider.autocomplete(any(EngineAutocompleteRequest.class)))
                .thenReturn(List.of("water leak", "water supply"));

        AutocompleteResponse response = searchQueryService.autocomplete(new AutocompleteRequest(
                "CMP_COMPLAINT", ORG_ID, null, "wat", "COMPLAINT", 10));

        assertThat(response.suggestions()).containsExactly("water leak", "water supply");

        ArgumentCaptor<EngineAutocompleteRequest> captor = ArgumentCaptor.forClass(EngineAutocompleteRequest.class);
        verify(searchEngineProvider).autocomplete(captor.capture());
        assertThat(captor.getValue().organizationId()).isEqualTo(ORG_ID);
        assertThat(captor.getValue().limit()).isEqualTo(10);

        ArgumentCaptor<SearchQueryHistoryDto> historyCaptor = ArgumentCaptor.forClass(SearchQueryHistoryDto.class);
        verify(searchQueryHistoryService).record(historyCaptor.capture());
        assertThat(historyCaptor.getValue().queryType()).isEqualTo(SearchQueryType.AUTOCOMPLETE);
    }

    @Test
    void shouldRejectAutocompleteLimitAboveTen() {
        assertThatThrownBy(() -> searchQueryService.autocomplete(new AutocompleteRequest(
                "CMP_COMPLAINT", ORG_ID, null, "wat", null, 11)))
                .isInstanceOf(SearchQueryException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void shouldSuggestUsingEngineProvider() {
        when(searchEngineProvider.suggest(any())).thenReturn(List.of("water"));

        List<String> suggestions = searchQueryService.suggest(new AutocompleteRequest(
                "CMP_COMPLAINT", ORG_ID, null, "wat", null, null));

        assertThat(suggestions).containsExactly("water");
    }
}
