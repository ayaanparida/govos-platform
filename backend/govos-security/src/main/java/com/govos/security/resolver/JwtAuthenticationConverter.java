package com.govos.security.resolver;

import com.govos.security.jwt.JwtAuthentication;
import com.govos.security.jwt.JwtTokenFactory;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

/**
 * Converts a validated bearer JWT into a {@link JwtAuthentication} for the security context.
 */
@Component
public class JwtAuthenticationConverter {

    private final JwtTokenFactory jwtTokenFactory;

    public JwtAuthenticationConverter(JwtTokenFactory jwtTokenFactory) {
        this.jwtTokenFactory = jwtTokenFactory;
    }

    public JwtAuthentication convert(String token) {
        Claims claims = jwtTokenFactory.parseClaims(token);
        var principal = jwtTokenFactory.toPrincipal(claims, token);
        return new JwtAuthentication(principal, token, principal.getAuthorities());
    }
}
