package com.govos.security.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.security.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GovosAuthenticationEntryPointTest {

    private ObjectMapper objectMapper;
    private GovosAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        entryPoint = new GovosAuthenticationEntryPoint(new SecurityResponseWriter(objectMapper));
    }

    @Test
    void shouldWriteUnauthorizedApiEnvelope() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test/protected");
        request.addHeader(SecurityConstants.REQUEST_ID_HEADER, "req-401");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Invalid token"));

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);

        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("requestId").asText()).isEqualTo("req-401");
        assertThat(body.get("data").get("code").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(body.get("data").get("message").asText()).isEqualTo("Authentication required");
        assertThat(body.get("data").get("path").asText()).isEqualTo("/api/v1/test/protected");
    }
}
