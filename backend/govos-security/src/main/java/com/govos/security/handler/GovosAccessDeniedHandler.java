package com.govos.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GovosAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityResponseWriter securityResponseWriter;

    public GovosAccessDeniedHandler(SecurityResponseWriter securityResponseWriter) {
        this.securityResponseWriter = securityResponseWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        securityResponseWriter.writeError(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "FORBIDDEN",
                "Access denied");
    }
}
