package com.govos.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.security.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GovosAccessDeniedHandlerTest {

    private ObjectMapper objectMapper;
    private GovosAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        accessDeniedHandler = new GovosAccessDeniedHandler(new SecurityResponseWriter(objectMapper));
    }

    @Test
    void shouldWriteForbiddenApiEnvelope() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/admin/users");
        request.setAttribute(SecurityConstants.REQUEST_ID_ATTRIBUTE, "req-403");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("requestId").asText()).isEqualTo("req-403");
        assertThat(body.get("data").get("code").asText()).isEqualTo("FORBIDDEN");
        assertThat(body.get("data").get("message").asText()).isEqualTo("Access denied");
    }
}
