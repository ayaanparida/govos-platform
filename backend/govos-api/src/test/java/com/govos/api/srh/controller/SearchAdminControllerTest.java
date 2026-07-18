package com.govos.api.srh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.admin.SearchClusterInfoDto;
import com.govos.srh.admin.SearchDashboardDto;
import com.govos.srh.admin.SearchHealthDto;
import com.govos.srh.admin.SearchIndexStatisticsDto;
import com.govos.srh.admin.SearchQueryStatisticsDto;
import com.govos.srh.admin.SearchSlowQueryDto;
import com.govos.srh.admin.SearchStatisticsDto;
import com.govos.srh.admin.SearchTopQueryDto;
import com.govos.srh.admin.SearchSemanticInfoDto;
import com.govos.srh.dto.SearchSyncJobDto;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SearchAdminControllerTest {

    private static final UUID INDEX_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID JOB_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String INDEX_CODE = "CMP_COMPLAINT";
    private static final String ALIAS_NAME = "cmp-complaint-read";

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
    void shouldReturnClusterHealth() throws Exception {
        when(searchApplicationService.getClusterHealth()).thenReturn(new SearchHealthDto(
                "UP", 2, 4, 4, 1024L, null, 512L, null, 15.0, Instant.now()));

        mockMvc.perform(get("/api/v1/search/admin/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.nodeCount").value(2));
    }

    @Test
    void shouldReturnClusterInformation() throws Exception {
        when(searchApplicationService.getClusterInformation()).thenReturn(new SearchClusterInfoDto(
                "govos-search", "green", 2, 4, 8, 0, 0, 0));

        mockMvc.perform(get("/api/v1/search/admin/cluster"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clusterName").value("govos-search"))
                .andExpect(jsonPath("$.data.status").value("green"));
    }

    @Test
    void shouldReturnPlatformStatistics() throws Exception {
        when(searchApplicationService.getSearchStatistics()).thenReturn(new SearchStatisticsDto(
                3, 2, 150, 500, 125.5, 1, 2, Instant.now()));

        mockMvc.perform(get("/api/v1/search/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalIndexes").value(3))
                .andExpect(jsonPath("$.data.totalQueries").value(500));
    }

    @Test
    void shouldReturnDashboard() throws Exception {
        SearchHealthDto health = new SearchHealthDto(
                "UP", 1, 2, 2, null, null, null, null, null, Instant.now());
        SearchQueryStatisticsDto queryStats = new SearchQueryStatisticsDto(10, 90.0, 5, 8, List.of());
        SearchStatisticsDto platformStats = new SearchStatisticsDto(1, 1, 20, 10, 90.0, 0, 0, Instant.now());

        when(searchApplicationService.getSearchDashboard()).thenReturn(new SearchDashboardDto(
                health,
                queryStats,
                platformStats,
                List.of(),
                List.of(),
                List.of(),
                new SearchSemanticInfoDto("mock", 384, false, "UP", "UP", 0L, "mock", 1, "UP", 0L),
                Instant.now()));

        mockMvc.perform(get("/api/v1/search/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.health.status").value("UP"))
                .andExpect(jsonPath("$.data.queryStatistics.totalQueries").value(10))
                .andExpect(jsonPath("$.data.semanticInfo.provider").value("mock"));
    }

    @Test
    void shouldReturnIndexStatistics() throws Exception {
        when(searchApplicationService.getIndexStatistics(INDEX_ID))
                .thenReturn(new SearchIndexStatisticsDto(
                        INDEX_ID,
                        INDEX_CODE,
                        100,
                        2,
                        8192L,
                        Instant.parse("2026-01-02T01:00:00Z"),
                        Instant.parse("2026-01-05T00:00:00Z"),
                        ALIAS_NAME,
                        2,
                        45));

        mockMvc.perform(get("/api/v1/search/admin/indexes/{id}/statistics", INDEX_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indexCode").value(INDEX_CODE))
                .andExpect(jsonPath("$.data.documentCount").value(100));
    }

    @Test
    void shouldReturnTopQueries() throws Exception {
        when(searchApplicationService.getTopQueries(5)).thenReturn(List.of(
                new SearchTopQueryDto("water leak", 12, 110.0)));

        mockMvc.perform(get("/api/v1/search/admin/queries/top").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].queryText").value("water leak"))
                .andExpect(jsonPath("$.data[0].count").value(12));
    }

    @Test
    void shouldReturnSlowQueries() throws Exception {
        when(searchApplicationService.getSlowQueries(5)).thenReturn(List.of(
                new SearchSlowQueryDto("complex query", ORG_ID, 1500L, Instant.now())));

        mockMvc.perform(get("/api/v1/search/admin/queries/slow").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].executionTimeMs").value(1500));
    }

    @Test
    void shouldStartSingleIndexReindex() throws Exception {
        SearchSyncJobDto job = new SearchSyncJobDto(
                JOB_ID,
                "JOB-001",
                INDEX_ID,
                "Reindex CMP_COMPLAINT",
                SearchJobType.FULL_REINDEX,
                SearchJobStatus.COMPLETED,
                Instant.now(),
                Instant.now(),
                0L,
                0L,
                0L,
                null,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());

        when(searchApplicationService.reindexIndex(INDEX_ID)).thenReturn(job);

        mockMvc.perform(post("/api/v1/search/admin/indexes/{id}/reindex", INDEX_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.jobStatus").value("COMPLETED"));

        verify(searchApplicationService).reindexIndex(eq(INDEX_ID));
    }

    @Test
    void shouldStartPlatformReindex() throws Exception {
        when(searchApplicationService.reindexAll()).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/search/admin/reindex-all"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true));

        verify(searchApplicationService).reindexAll();
    }

    @Test
    void shouldCancelReindexJob() throws Exception {
        SearchSyncJobDto cancelled = new SearchSyncJobDto(
                JOB_ID,
                "JOB-001",
                INDEX_ID,
                "Reindex",
                SearchJobType.FULL_REINDEX,
                SearchJobStatus.CANCELLED,
                Instant.now(),
                Instant.now(),
                0L,
                0L,
                0L,
                null,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());

        when(searchApplicationService.cancelReindex(JOB_ID)).thenReturn(cancelled);

        mockMvc.perform(post("/api/v1/search/admin/jobs/{id}/cancel", JOB_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.jobStatus").value("CANCELLED"));
    }
}
