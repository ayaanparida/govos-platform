package com.govos.api.srh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.ai.HybridSearchRequest;
import com.govos.srh.ai.SemanticSearchException;
import com.govos.srh.ai.SemanticSearchRequest;
import com.govos.srh.query.SearchPage;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SemanticSearchControllerTest {

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private SearchApplicationService searchApplicationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

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
    void shouldExecuteSemanticSearch() throws Exception {
        when(searchApplicationService.semanticSearch(any(SemanticSearchRequest.class)))
                .thenReturn(sampleResponse());

        SemanticSearchRequest request = new SemanticSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water leak", null, SearchPage.defaults(), null);

        mockMvc.perform(post("/api/v1/search/semantic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalHits").value(1));

        verify(searchApplicationService).semanticSearch(any(SemanticSearchRequest.class));
    }

    @Test
    void shouldExecuteHybridSearch() throws Exception {
        when(searchApplicationService.hybridSearch(any(HybridSearchRequest.class)))
                .thenReturn(sampleResponse());

        HybridSearchRequest request = new HybridSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water leak", null, null,
                SearchPage.defaults(), null, false, null, null, 0.70, 0.30);

        mockMvc.perform(post("/api/v1/search/hybrid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results[0].id").value("doc-1"));

        verify(searchApplicationService).hybridSearch(any(HybridSearchRequest.class));
    }

    @Test
    void shouldReturnErrorWhenSemanticSearchDisabled() throws Exception {
        when(searchApplicationService.semanticSearch(any(SemanticSearchRequest.class)))
                .thenThrow(new SemanticSearchException("Semantic search is disabled"));

        SemanticSearchRequest request = new SemanticSearchRequest(
                "CMP_COMPLAINT", ORG_ID, null, "water leak", null, SearchPage.defaults(), null);

        mockMvc.perform(post("/api/v1/search/semantic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity());
    }

    private SearchResponse sampleResponse() {
        return new SearchResponse(
                1,
                List.of(new SearchResult("doc-1", 0.92, Map.of("title", "water leak"), null)),
                List.of(),
                SearchPage.defaults(),
                15L);
    }
}
