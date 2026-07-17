package com.govos.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.security.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class SecurityResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String code,
            String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String requestId = resolveRequestId(request);
        SecurityErrorResponse error = new SecurityErrorResponse(
                code,
                message,
                request.getRequestURI(),
                Instant.now(),
                requestId,
                null);

        SecurityApiResponse<SecurityErrorResponse> body = new SecurityApiResponse<>(
                false,
                error,
                message,
                Instant.now(),
                requestId);

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object attribute = request.getAttribute(SecurityConstants.REQUEST_ID_ATTRIBUTE);
        if (attribute instanceof String requestId && !requestId.isBlank()) {
            return requestId;
        }
        String header = request.getHeader(SecurityConstants.REQUEST_ID_HEADER);
        return header != null && !header.isBlank() ? header : null;
    }
}
