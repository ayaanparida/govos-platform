package com.govos.api.srh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.exception.SearchQueryException;
import com.govos.srh.query.AutocompleteRequest;
import com.govos.srh.query.AutocompleteResponse;
import com.govos.srh.query.FacetBucket;
import com.govos.srh.query.FacetResult;
import com.govos.srh.query.FacetSearchRequest;
import com.govos.srh.query.GeoSearchRequest;
import com.govos.srh.query.SearchPage;
import com.govos.srh.query.SearchQueryMode;
import com.govos.srh.query.SearchRequest;
import com.govos.srh.query.SearchResponse;
import com.govos.srh.query.SearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchQueryControllerTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        SearchController controller = new SearchController(searchApplicationService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldExecuteSearchQuery() throws Exception {
        when(searchApplicationService.search(any(SearchRequest.class))).thenReturn(sampleSearchResponse());

        SearchRequest request = new SearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water leak", SearchQueryMode.FULL_TEXT,
                null, new SearchPage(0, 20), null, true, null);

        mockMvc.perform(post("/api/v1/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalHits").value(1))
                .andExpect(jsonPath("$.data.results[0].id").value("doc-1"));

        verify(searchApplicationService).search(any(SearchRequest.class));
    }

    @Test
    void shouldExecuteAutocompleteQuery() throws Exception {
        when(searchApplicationService.autocomplete(any(AutocompleteRequest.class)))
                .thenReturn(new AutocompleteResponse(List.of("water leak"), 12L));

        AutocompleteRequest request = new AutocompleteRequest(
                "CMP_COMPLAINT", ORG_ID, null, "wat", "COMPLAINT", 10);

        mockMvc.perform(post("/api/v1/search/autocomplete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.suggestions[0]").value("water leak"));
    }

    @Test
    void shouldExecuteFacetQuery() throws Exception {
        when(searchApplicationService.facetSearch(any(FacetSearchRequest.class)))
                .thenReturn(new SearchResponse(
                        3,
                        List.of(),
                        List.of(new FacetResult("status", List.of(new FacetBucket("OPEN", 3)))),
                        new SearchPage(0, 0),
                        8L));

        FacetSearchRequest request = new FacetSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water", null, List.of("status"));

        mockMvc.perform(post("/api/v1/search/facets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facets[0].name").value("status"));
    }

    @Test
    void shouldExecuteGeoQuery() throws Exception {
        when(searchApplicationService.geoSearch(any(GeoSearchRequest.class)))
                .thenReturn(sampleSearchResponse());

        GeoSearchRequest request = new GeoSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                5.0, null, null, null, null,
                "leak", null, new SearchPage(0, 20), true);

        mockMvc.perform(post("/api/v1/search/geo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalHits").value(1));
    }

    @Test
    void shouldReturnSearchCount() throws Exception {
        when(searchApplicationService.count(any(SearchRequest.class))).thenReturn(42L);

        mockMvc.perform(get("/api/v1/search/count")
                        .param("indexCode", "CMP_COMPLAINT")
                        .param("organizationId", ORG_ID.toString())
                        .param("queryText", "water"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(42));
    }

    @Test
    void shouldMapSearchQueryValidationFailureTo422() throws Exception {
        when(searchApplicationService.search(any(SearchRequest.class)))
                .thenThrow(new SearchQueryException("Organization id is required for search"));

        SearchRequest request = new SearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water", null,
                null, new SearchPage(0, 20), null, false, null);

        mockMvc.perform(post("/api/v1/search/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SEARCH_ERROR"));
    }

    private static SearchResponse sampleSearchResponse() {
        return new SearchResponse(
                1,
                List.of(new SearchResult("doc-1", 1.2, Map.of("title", "Water leak"), null)),
                List.of(),
                new SearchPage(0, 20),
                15L);
    }
}
