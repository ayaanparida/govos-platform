package com.govos.api.srh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.srh.scheduler.SearchScheduledJobRecordDto;
import com.govos.srh.scheduler.SearchSchedulerStatusDto;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SchedulerControllerTest {

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
    void shouldReturnSchedulerStatus() throws Exception {
        when(searchApplicationService.getSchedulerStatus()).thenReturn(new SearchSchedulerStatusDto(
                true, "0 0 2 * * *", "0 0 */6 * * *", "0 30 3 * * *",
                "0 0 4 * * *", "0 */15 * * * *", "0 5 * * * *",
                3, 5L, 1L, List.of("daily-full-reindex")));

        mockMvc.perform(get("/api/v1/search/admin/scheduler"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void shouldTriggerSchedulerReindex() throws Exception {
        UUID recordId = UUID.randomUUID();
        when(searchApplicationService.triggerSchedulerReindex(true)).thenReturn(new SearchScheduledJobRecordDto(
                recordId, "daily-full-reindex", "COMPLETED", Instant.now(), Instant.now(),
                100L, null, 2L, 0));

        mockMvc.perform(post("/api/v1/search/admin/scheduler/reindex")
                        .param("full", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.jobName").value("daily-full-reindex"));

        verify(searchApplicationService).triggerSchedulerReindex(true);
    }

    @Test
    void shouldReturnSchedulerHistory() throws Exception {
        when(searchApplicationService.getSchedulerHistory(50)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/search/admin/scheduler/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
}
