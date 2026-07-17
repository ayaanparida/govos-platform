package com.govos.security.matcher;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

/**
 * Matches platform endpoints that do not require JWT authentication.
 */
@Component
public class PublicEndpointMatcher {

    public static final String[] PUBLIC_PATTERNS = {
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    };

    private final RequestMatcher requestMatcher;

    public PublicEndpointMatcher() {
        RequestMatcher[] matchers = new RequestMatcher[PUBLIC_PATTERNS.length];
        for (int i = 0; i < PUBLIC_PATTERNS.length; i++) {
            String pattern = PUBLIC_PATTERNS[i];
            if ("/api/v1/auth/login".equals(pattern) || "/api/v1/auth/refresh".equals(pattern)) {
                matchers[i] = new AntPathRequestMatcher(pattern, HttpMethod.POST.name());
            } else {
                matchers[i] = new AntPathRequestMatcher(pattern);
            }
        }
        this.requestMatcher = new OrRequestMatcher(matchers);
    }

    public boolean matches(HttpServletRequest request) {
        return requestMatcher.matches(request);
    }

    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }
}
