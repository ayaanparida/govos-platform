package com.govos.security.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.security.constant.SecurityConstants;
import com.govos.security.jwt.JwtTokenFactory;
import com.govos.security.support.SecurityWebTestApplication;
import com.govos.security.support.TestSecuredController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {SecurityWebTestApplication.class, TestSecuredController.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "govos.security.jwt.secret=govos-test-jwt-secret-key-minimum-sixty-four-bytes-long-for-hs512-signing!!",
        "govos.security.jwt.issuer=govos-test"
})
class SecurityFilterChainConfigurationTest {

    private static final UUID USER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenFactory jwtTokenFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPermitPublicLoginWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldPermitActuatorWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                java.util.List.of("OFFICER"),
                java.util.List.of("idm:user:read"),
                "session-123");

        mockMvc.perform(get("/api/v1/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldResolveCurrentUserFromJwtPrincipal() throws Exception {
        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                java.util.List.of("OFFICER"),
                java.util.List.of("idm:user:read"),
                "session-123");

        mockMvc.perform(get("/api/v1/test/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.username").value("jdoe"));
    }

    @Test
    void shouldReturnApiEnvelopeOnUnauthorizedWithRequestId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/test/protected")
                        .header(SecurityConstants.REQUEST_ID_HEADER, "req-123"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("success").asBoolean()).isFalse();
        assertThat(body.get("requestId").asText()).isEqualTo("req-123");
        assertThat(body.get("data").get("code").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(result.getResponse().getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
