package com.govos.api.srh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.dto.SearchIndexCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.exception.SearchIndexValidationException;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final UUID INDEX_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

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
    void shouldCreateSearchIndex() throws Exception {
        SearchIndexCreateRequest request = sampleCreateRequest();
        when(searchApplicationService.createIndex(any(SearchIndexCreateRequest.class)))
                .thenReturn(sampleIndexDto());

        mockMvc.perform(post("/api/v1/search/indexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(INDEX_ID.toString()));
    }

    @Test
    void shouldReturnSearchIndexById() throws Exception {
        when(searchApplicationService.getIndex(INDEX_ID)).thenReturn(sampleIndexDto());

        mockMvc.perform(get("/api/v1/search/indexes/{id}", INDEX_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CMP_COMPLAINT"));
    }

    @Test
    void shouldSoftDeleteSearchIndex() throws Exception {
        mockMvc.perform(delete("/api/v1/search/indexes/{id}", INDEX_ID))
                .andExpect(status().isNoContent());

        verify(searchApplicationService).softDeleteIndex(INDEX_ID);
    }

    @Test
    void shouldActivateSearchIndex() throws Exception {
        when(searchApplicationService.activateIndex(INDEX_ID)).thenReturn(sampleIndexDto());

        mockMvc.perform(post("/api/v1/search/indexes/{id}/activate", INDEX_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(searchApplicationService).activateIndex(INDEX_ID);
    }

    @Test
    void shouldReturn400WhenCreateValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/search/indexes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn404WhenSearchIndexNotFound() throws Exception {
        when(searchApplicationService.getIndex(INDEX_ID))
                .thenThrow(new SearchIndexNotFoundException("Search index not found with id: " + INDEX_ID));

        mockMvc.perform(get("/api/v1/search/indexes/{id}", INDEX_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturn422WhenBusinessValidationFails() throws Exception {
        when(searchApplicationService.activateIndex(INDEX_ID))
                .thenThrow(new SearchIndexValidationException("Search index cannot be activated"));

        mockMvc.perform(post("/api/v1/search/indexes/{id}/activate", INDEX_ID))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("SEARCH_ERROR"));
    }

    private static SearchIndexCreateRequest sampleCreateRequest() {
        return new SearchIndexCreateRequest(
                "CMP_COMPLAINT",
                "Complaint Index",
                "CMP complaints",
                SearchEngineType.OPENSEARCH,
                1,
                "cmp-complaint-alias",
                true);
    }

    private static SearchIndexDto sampleIndexDto() {
        return new SearchIndexDto(
                INDEX_ID,
                "CMP_COMPLAINT",
                "Complaint Index",
                "CMP complaints",
                SearchEngineType.OPENSEARCH,
                SearchIndexStatus.ACTIVE,
                1,
                "cmp-complaint-v1",
                "cmp-complaint-alias",
                0L,
                null,
                true,
                0L,
                null,
                null,
                null,
                null);
    }
}
