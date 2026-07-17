package com.govos.security.filter;

import com.govos.security.handler.GovosAuthenticationEntryPoint;
import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtInvalidSignatureException;
import com.govos.security.jwt.JwtPrincipal;
import com.govos.security.matcher.PublicEndpointMatcher;
import com.govos.security.resolver.JwtAuthenticationConverter;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final UUID USER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    @Mock
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @Mock
    private PublicEndpointMatcher publicEndpointMatcher;

    @Mock
    private GovosAuthenticationEntryPoint authenticationEntryPoint;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(
                jwtAuthenticationConverter,
                publicEndpointMatcher,
                authenticationEntryPoint);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipAuthenticationForPublicEndpoints() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(publicEndpointMatcher.matches(request)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        verify(jwtAuthenticationConverter, never()).convert(any());
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldPopulateSecurityContextForValidBearerToken() throws Exception {
        MockHttpServletRequest request = bearerRequest("valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JwtPrincipal principal = new JwtPrincipal(
                USER_ID,
                "jdoe",
                "session-1",
                "jti-1",
                List.of(),
                "valid-token");
        JwtAuthentication authentication = new JwtAuthentication(principal, "valid-token", List.of());

        when(publicEndpointMatcher.matches(request)).thenReturn(false);
        when(jwtAuthenticationConverter.convert("valid-token")).thenReturn(authentication);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldDelegateToEntryPointWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = bearerRequest("bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(publicEndpointMatcher.matches(request)).thenReturn(false);
        when(jwtAuthenticationConverter.convert("bad-token"))
                .thenThrow(new JwtInvalidSignatureException("Invalid signature", new RuntimeException("bad sig")));

        filter.doFilter(request, response, filterChain);

        verify(authenticationEntryPoint).commence(
                eq(request),
                eq(response),
                any(BadCredentialsException.class));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldContinueChainWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(publicEndpointMatcher.matches(request)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(jwtAuthenticationConverter, never()).convert(any());
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/test/protected");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
