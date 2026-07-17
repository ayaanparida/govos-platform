package com.govos.security.filter;

import com.govos.security.constant.SecurityConstants;
import com.govos.security.handler.GovosAuthenticationEntryPoint;
import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtException;
import com.govos.security.matcher.PublicEndpointMatcher;
import com.govos.security.resolver.JwtAuthenticationConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates bearer JWT access tokens and populates the {@link SecurityContextHolder}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final PublicEndpointMatcher publicEndpointMatcher;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            PublicEndpointMatcher publicEndpointMatcher,
            GovosAuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.publicEndpointMatcher = publicEndpointMatcher;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (publicEndpointMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveBearerToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtAuthentication authentication = jwtAuthenticationConverter.convert(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute(
                    SecurityConstants.AUTHENTICATED_USERNAME_ATTRIBUTE,
                    authentication.getPrincipal().getUsername());
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Invalid JWT access token", ex));
        }
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (header == null || !header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(SecurityConstants.BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
