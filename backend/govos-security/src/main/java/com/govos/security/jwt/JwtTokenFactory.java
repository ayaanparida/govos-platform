package com.govos.security.jwt;

import com.govos.security.config.SecurityProperties;
import com.govos.security.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Core JWT creation, parsing, and authority extraction using JJWT (HS512).
 */
@Component
public class JwtTokenFactory {

    private final SecurityProperties securityProperties;
    private final SecretKey signingKey;
    private final Clock clock;

    public JwtTokenFactory(SecurityProperties securityProperties) {
        this(securityProperties, Clock.systemUTC());
    }

    JwtTokenFactory(SecurityProperties securityProperties, Clock clock) {
        this.securityProperties = securityProperties;
        this.clock = clock;
        this.signingKey = buildSigningKey(securityProperties.getJwt().getSecret());
    }

    public String generateAccessToken(
            UUID userId,
            String username,
            List<String> roles,
            List<String> permissions,
            String sessionId) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(securityProperties.getJwt().getAccessTokenTtl());
        String jti = UUID.randomUUID().toString();

        List<String> embeddedPermissions = resolveEmbeddedPermissions(permissions);

        return Jwts.builder()
                .issuer(securityProperties.getJwt().getIssuer())
                .subject(userId.toString())
                .id(jti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim(JwtConstants.CLAIM_USER_ID, userId.toString())
                .claim(JwtConstants.CLAIM_USERNAME, username)
                .claim(JwtConstants.CLAIM_ROLES, roles)
                .claim(JwtConstants.CLAIM_PERMISSIONS, embeddedPermissions)
                .claim(JwtConstants.CLAIM_SESSION_ID, sessionId)
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Package-visible helper for unit tests requiring custom token lifetimes.
     */
    String generateAccessToken(
            UUID userId,
            String username,
            List<String> roles,
            List<String> permissions,
            String sessionId,
            Instant issuedAt,
            Instant expiresAt) {
        String jti = UUID.randomUUID().toString();
        List<String> embeddedPermissions = resolveEmbeddedPermissions(permissions);

        return Jwts.builder()
                .issuer(securityProperties.getJwt().getIssuer())
                .subject(userId.toString())
                .id(jti)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claim(JwtConstants.CLAIM_USER_ID, userId.toString())
                .claim(JwtConstants.CLAIM_USERNAME, username)
                .claim(JwtConstants.CLAIM_ROLES, roles)
                .claim(JwtConstants.CLAIM_PERMISSIONS, embeddedPermissions)
                .claim(JwtConstants.CLAIM_SESSION_ID, sessionId)
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(securityProperties.getJwt().getIssuer())
                    .clockSkewSeconds(securityProperties.getJwt().getClockSkew().getSeconds())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new JwtExpiredException("JWT access token expired", ex);
        } catch (SignatureException ex) {
            throw new JwtInvalidSignatureException("JWT signature validation failed", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtMalformedTokenException("JWT is malformed", ex);
        } catch (io.jsonwebtoken.JwtException ex) {
            throw new JwtMalformedTokenException("JWT parsing failed", ex);
        }
    }

    public Collection<GrantedAuthority> extractAuthorities(Claims claims) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (String role : readStringList(claims, JwtConstants.CLAIM_ROLES)) {
            if (role == null || role.isBlank()) {
                continue;
            }
            String authority = role.startsWith(SecurityConstants.ROLE_PREFIX)
                    ? role
                    : SecurityConstants.ROLE_PREFIX + role;
            authorities.add(new SimpleGrantedAuthority(authority));
        }

        for (String permission : readStringList(claims, JwtConstants.CLAIM_PERMISSIONS)) {
            if (permission != null && !permission.isBlank()) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }

        return Collections.unmodifiableList(authorities);
    }

    public JwtPrincipal toPrincipal(Claims claims, String rawToken) {
        UUID userId = UUID.fromString(claims.getSubject());
        String username = claims.get(JwtConstants.CLAIM_USERNAME, String.class);
        String sessionId = claims.get(JwtConstants.CLAIM_SESSION_ID, String.class);
        Collection<GrantedAuthority> authorities = extractAuthorities(claims);

        return new JwtPrincipal(
                userId,
                username,
                sessionId,
                claims.getId(),
                authorities,
                rawToken);
    }

    private List<String> resolveEmbeddedPermissions(List<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return List.of();
        }
        int threshold = securityProperties.getJwt().getPermissionEmbedThreshold();
        if (permissions.size() > threshold) {
            return List.of();
        }
        return List.copyOf(permissions);
    }

    @SuppressWarnings("unchecked")
    private List<String> readStringList(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of(String.valueOf(value));
    }

    static SecretKey buildSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("govos.security.jwt.secret must be configured");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < JwtConstants.MIN_SECRET_BYTES_HS512) {
            throw new IllegalStateException(
                    "govos.security.jwt.secret must be at least "
                            + JwtConstants.MIN_SECRET_BYTES_HS512 + " bytes for HS512");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
