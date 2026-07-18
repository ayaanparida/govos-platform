package com.govos.api.srh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.observability.SearchErrorSnapshotDto;
import com.govos.srh.observability.SearchLatencySnapshotDto;
import com.govos.srh.observability.SearchMetricsSnapshotDto;
import com.govos.srh.observability.SearchObservabilitySnapshotDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ObservationControllerTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SearchController controller = new SearchController(searchApplicationService);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldReturnObservabilitySnapshot() throws Exception {
        when(searchApplicationService.getObservabilitySnapshot()).thenReturn(new SearchObservabilitySnapshotDto(
                true, "otlp", "http://localhost:4317", 1.0, 3L, 5L, "mock", "opensearch"));

        mockMvc.perform(get("/api/v1/search/admin/observability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.activeEngine").value("opensearch"));
    }

    @Test
    void shouldReturnMetricsSnapshot() throws Exception {
        when(searchApplicationService.getObservabilityMetrics()).thenReturn(new SearchMetricsSnapshotDto(
                10L, 8L, 2L, 15L, 0.2, 0.16));

        mockMvc.perform(get("/api/v1/search/admin/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTraces").value(10));
    }

    @Test
    void shouldReturnLatencySnapshot() throws Exception {
        when(searchApplicationService.getObservabilityLatency()).thenReturn(new SearchLatencySnapshotDto(
                12.0, 45.0, 80.0, 120.0, 200.0, 90.0));

        mockMvc.perform(get("/api/v1/search/admin/latency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.queryLatencyMs").value(12.0));
    }

    @Test
    void shouldReturnErrorSnapshot() throws Exception {
        when(searchApplicationService.getObservabilityErrors()).thenReturn(new SearchErrorSnapshotDto(
                2L, 0.2, List.of("search.query:2")));

        mockMvc.perform(get("/api/v1/search/admin/errors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalErrors").value(2));

        verify(searchApplicationService).getObservabilityErrors();
    }

    @Test
    void shouldReturnTraces() throws Exception {
        when(searchApplicationService.getObservabilityTraces(50)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/search/admin/traces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
