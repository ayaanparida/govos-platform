package com.govos.security.jwt;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class JwtClaimsExtractorImpl implements JwtClaimsExtractor {

    private final JwtTokenFactory jwtTokenFactory;

    public JwtClaimsExtractorImpl(JwtTokenFactory jwtTokenFactory) {
        this.jwtTokenFactory = jwtTokenFactory;
    }

    @Override
    public UUID extractUserId(String token) {
        return UUID.fromString(jwtTokenFactory.parseClaims(token).getSubject());
    }

    @Override
    public String extractUsername(String token) {
        return jwtTokenFactory.parseClaims(token).get(JwtConstants.CLAIM_USERNAME, String.class);
    }

    @Override
    public String extractSessionId(String token) {
        return jwtTokenFactory.parseClaims(token).get(JwtConstants.CLAIM_SESSION_ID, String.class);
    }

    @Override
    public List<String> extractRoles(String token) {
        return readStringList(jwtTokenFactory.parseClaims(token), JwtConstants.CLAIM_ROLES);
    }

    @Override
    public List<String> extractPermissions(String token) {
        return readStringList(jwtTokenFactory.parseClaims(token), JwtConstants.CLAIM_PERMISSIONS);
    }

    @SuppressWarnings("unchecked")
    private List<String> readStringList(io.jsonwebtoken.Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (value == null) {
            return List.of();
        }
        return List.of(String.valueOf(value));
    }
}
