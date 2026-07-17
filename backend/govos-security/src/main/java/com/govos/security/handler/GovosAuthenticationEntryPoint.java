package com.govos.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GovosAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityResponseWriter securityResponseWriter;

    public GovosAuthenticationEntryPoint(SecurityResponseWriter securityResponseWriter) {
        this.securityResponseWriter = securityResponseWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        securityResponseWriter.writeError(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication required");
    }
}
