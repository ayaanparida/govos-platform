package com.govos.security.filter;

import com.govos.security.constant.SecurityConstants;
import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs request correlation metadata and response timing for secured HTTP traffic.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityRequestLoggingFilter.class);
    static final String START_TIME_ATTRIBUTE = "govos.security.requestStartMs";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        request.setAttribute(START_TIME_ATTRIBUTE, start);

        String requestId = resolveRequestId(request);
        if (requestId != null) {
            request.setAttribute(SecurityConstants.REQUEST_ID_ATTRIBUTE, requestId);
            MDC.put("requestId", requestId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            log.info(
                    "requestId={} username={} uri={} status={} durationMs={}",
                    requestId,
                    resolveUsername(request),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);
            MDC.remove("requestId");
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        Object attribute = request.getAttribute(SecurityConstants.REQUEST_ID_ATTRIBUTE);
        if (attribute instanceof String requestId && !requestId.isBlank()) {
            return requestId;
        }
        String header = request.getHeader(SecurityConstants.REQUEST_ID_HEADER);
        return header != null && !header.isBlank() ? header.trim() : null;
    }

    private String resolveUsername(HttpServletRequest request) {
        Object attribute = request.getAttribute(SecurityConstants.AUTHENTICATED_USERNAME_ATTRIBUTE);
        if (attribute instanceof String username && !username.isBlank()) {
            return username;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthentication jwtAuthentication) {
            JwtPrincipal principal = jwtAuthentication.getPrincipal();
            return principal.getUsername();
        }
        if (authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal) {
            return principal.getUsername();
        }
        return "anonymous";
    }
}
