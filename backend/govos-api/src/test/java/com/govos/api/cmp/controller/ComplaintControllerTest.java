package com.govos.api.cmp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.cmp.application.ComplaintApplicationService;
import com.govos.api.cmp.mapper.ComplaintApiMapper;
import com.govos.api.cmp.request.RejectComplaintRequest;
import com.govos.api.common.advice.GlobalExceptionHandler;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.exception.ComplaintNotFoundException;
import com.govos.cmp.exception.ComplaintValidationException;
import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtPrincipal;
import com.govos.security.resolver.CurrentUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ComplaintControllerTest {

    @Mock private ComplaintApplicationService complaintApplicationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private JwtAuthentication jwtAuthentication;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void setUp() {
        ComplaintController controller = new ComplaintController(
                complaintApplicationService,
                new ComplaintApiMapper());

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        jwtAuthentication = new JwtAuthentication(
                new JwtPrincipal(USER_ID, "officer", "session-1", "token-1", List.of(), "raw-token"),
                "raw-token",
                List.of());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldCreateComplaint() throws Exception {
        ComplaintCreateRequest request = sampleCreateRequest();
        when(complaintApplicationService.create(any(ComplaintCreateRequest.class))).thenReturn(sampleComplaintDto());

        mockMvc.perform(post("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwtUser()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(COMPLAINT_ID.toString()));
    }

    @Test
    void shouldReturnComplaintById() throws Exception {
        when(complaintApplicationService.getById(COMPLAINT_ID)).thenReturn(sampleComplaintDto());

        mockMvc.perform(get("/api/v1/complaints/{id}", COMPLAINT_ID)
                        .with(jwtUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("CMP-2026-0001"));
    }

    @Test
    void shouldSoftDeleteComplaint() throws Exception {
        mockMvc.perform(delete("/api/v1/complaints/{id}", COMPLAINT_ID)
                        .with(jwtUser()))
                .andExpect(status().isNoContent());

        verify(complaintApplicationService).softDelete(COMPLAINT_ID);
    }

    @Test
    void shouldSubmitComplaint() throws Exception {
        when(complaintApplicationService.submit(COMPLAINT_ID, USER_ID)).thenReturn(sampleComplaintDto());

        mockMvc.perform(post("/api/v1/complaints/{id}/submit", COMPLAINT_ID)
                        .with(jwtUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(complaintApplicationService).submit(COMPLAINT_ID, USER_ID);
    }

    @Test
    void shouldRejectComplaint() throws Exception {
        when(complaintApplicationService.reject(eq(COMPLAINT_ID), eq(USER_ID), eq("INVALID")))
                .thenReturn(sampleComplaintDto());

        mockMvc.perform(post("/api/v1/complaints/{id}/reject", COMPLAINT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RejectComplaintRequest("INVALID")))
                        .with(jwtUser()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenCreateValidationFails() throws Exception {
        mockMvc.perform(post("/api/v1/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","source":"CITIZEN_PORTAL"}
                                """)
                        .with(jwtUser()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturn404WhenComplaintNotFound() throws Exception {
        when(complaintApplicationService.getById(COMPLAINT_ID))
                .thenThrow(new ComplaintNotFoundException(COMPLAINT_ID));

        mockMvc.perform(get("/api/v1/complaints/{id}", COMPLAINT_ID)
                        .with(jwtUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturn422WhenBusinessValidationFails() throws Exception {
        when(complaintApplicationService.submit(COMPLAINT_ID, USER_ID))
                .thenThrow(new ComplaintValidationException("Complaint title is required"));

        mockMvc.perform(post("/api/v1/complaints/{id}/submit", COMPLAINT_ID)
                        .with(jwtUser()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("COMPLAINT_ERROR"));
    }

    private static ComplaintCreateRequest sampleCreateRequest() {
        return new ComplaintCreateRequest(
                "Water leak", "Description", ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null, null,
                "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null, true);
    }

    private static ComplaintDto sampleComplaintDto() {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                ComplaintStatus.DRAFT, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, null, null, null, null,
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null,
                true, 0L, null, null, null, null);
    }

    private RequestPostProcessor jwtUser() {
        return request -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(jwtAuthentication);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }
}
