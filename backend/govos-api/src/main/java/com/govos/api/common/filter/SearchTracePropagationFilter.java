package com.govos.api.common.filter;

import com.govos.srh.observability.SearchTraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Propagates distributed tracing identifiers into MDC for SRH observability.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SearchTracePropagationFilter extends OncePerRequestFilter {

    private static final String TRACE_HEADER = "X-Trace-ID";
    private static final String SPAN_HEADER = "X-Span-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveHeader(request, TRACE_HEADER, UUID.randomUUID().toString().replace("-", ""));
        String spanId = resolveHeader(request, SPAN_HEADER, generateSpanId());
        String requestId = MDC.get(SearchTraceContext.MDC_REQUEST_ID);

        SearchTraceContext.bootstrap(traceId, spanId, requestId);
        response.setHeader(TRACE_HEADER, traceId);
        response.setHeader(SPAN_HEADER, spanId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            SearchTraceContext.enrich(null, authentication.getName());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(SearchTraceContext.MDC_TRACE_ID);
            MDC.remove(SearchTraceContext.MDC_SPAN_ID);
            MDC.remove(SearchTraceContext.MDC_USER_ID);
        }
    }

    private static String resolveHeader(HttpServletRequest request, String header, String fallback) {
        String value = request.getHeader(header);
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return fallback;
    }

    private static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
