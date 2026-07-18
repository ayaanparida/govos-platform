package com.govos.security.jwt;

import com.govos.security.config.SecurityConfigurationProperties;
import com.govos.security.config.SecurityProperties;
import com.govos.security.constant.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenFactoryTest {

    private static final String TEST_SECRET =
            "govos-test-jwt-secret-key-minimum-sixty-four-bytes-long-for-hs512-signing!!";
    private static final UUID USER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private SecurityProperties securityProperties;
    private JwtTokenFactory jwtTokenFactory;
    private JwtTokenValidatorImpl jwtTokenValidator;
    private JwtClaimsExtractorImpl jwtClaimsExtractor;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        SecurityConfigurationProperties.Jwt jwt = new SecurityConfigurationProperties.Jwt();
        jwt.setSecret(TEST_SECRET);
        jwt.setIssuer("govos-test");
        jwt.setAccessTokenTtl(Duration.ofMinutes(15));
        jwt.setClockSkew(Duration.ofSeconds(60));
        jwt.setPermissionEmbedThreshold(50);
        securityProperties.setJwt(jwt);

        jwtTokenFactory = new JwtTokenFactory(securityProperties);
        jwtTokenValidator = new JwtTokenValidatorImpl(jwtTokenFactory);
        jwtClaimsExtractor = new JwtClaimsExtractorImpl(jwtTokenFactory);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER", "ADMIN"),
                List.of("idm:user:read", "idm:user:write"),
                "session-123");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenValidator.validateAccessToken(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() {
        Instant issuedAt = Instant.parse("2020-01-01T00:00:00Z");
        Instant expiresAt = Instant.parse("2020-01-01T00:15:00Z");

        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER"),
                List.of("idm:user:read"),
                "session-123",
                issuedAt,
                expiresAt);

        assertThat(jwtTokenValidator.validateAccessToken(token)).isFalse();
        assertThatThrownBy(() -> jwtTokenFactory.parseClaims(token))
                .isInstanceOf(JwtExpiredException.class);
    }

    @Test
    void shouldRejectInvalidSignature() {
        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER"),
                List.of(),
                "session-123");

        SecurityConfigurationProperties.Jwt otherJwt = new SecurityConfigurationProperties.Jwt();
        otherJwt.setSecret("another-govos-test-jwt-secret-key-minimum-sixty-four-bytes-long-for-hs512!!");
        otherJwt.setIssuer("govos-test");
        otherJwt.setClockSkew(Duration.ofSeconds(60));

        SecurityProperties otherProperties = new SecurityProperties();
        otherProperties.setJwt(otherJwt);
        JwtTokenFactory otherFactory = new JwtTokenFactory(otherProperties);

        assertThatThrownBy(() -> otherFactory.parseClaims(token))
                .isInstanceOf(JwtInvalidSignatureException.class);
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThat(jwtTokenValidator.validateAccessToken("not-a-valid-jwt")).isFalse();
        assertThatThrownBy(() -> jwtTokenFactory.parseClaims("not-a-valid-jwt"))
                .isInstanceOf(JwtMalformedTokenException.class);
    }

    @Test
    void shouldExtractClaims() {
        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER", "ADMIN"),
                List.of("idm:user:read"),
                "session-456");

        assertThat(jwtClaimsExtractor.extractUserId(token)).isEqualTo(USER_ID);
        assertThat(jwtClaimsExtractor.extractUsername(token)).isEqualTo("jdoe");
        assertThat(jwtClaimsExtractor.extractSessionId(token)).isEqualTo("session-456");
        assertThat(jwtClaimsExtractor.extractRoles(token)).containsExactly("OFFICER", "ADMIN");
        assertThat(jwtClaimsExtractor.extractPermissions(token)).containsExactly("idm:user:read");
    }

    @Test
    void shouldExtractAuthoritiesFromClaims() {
        var claims = jwtTokenFactory.parseClaims(jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER"),
                List.of("idm:user:read"),
                "session-789"));

        List<String> authorities = jwtTokenFactory.extractAuthorities(claims).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertThat(authorities).containsExactly(
                SecurityConstants.ROLE_PREFIX + "OFFICER",
                "idm:user:read");
    }

    @Test
    void shouldBuildJwtPrincipalAndAuthentication() {
        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER"),
                List.of("idm:user:read"),
                "session-000");

        JwtPrincipal principal = jwtTokenFactory.toPrincipal(jwtTokenFactory.parseClaims(token), token);
        JwtAuthentication authentication = new JwtAuthentication(
                principal,
                token,
                principal.getAuthorities());

        assertThat(principal.getUserId()).isEqualTo(USER_ID);
        assertThat(principal.getUsername()).isEqualTo("jdoe");
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isEqualTo(principal);
    }

    @Test
    void shouldGenerateOpaqueRefreshTokenValue() {
        String refreshToken = jwtTokenFactory.generateRefreshTokenValue();

        assertThat(refreshToken).isNotBlank();
        assertThat(UUID.fromString(refreshToken)).isNotNull();
    }

    @Test
    void shouldOmitPermissionsWhenAboveThreshold() {
        securityProperties.getJwt().setPermissionEmbedThreshold(2);

        String token = jwtTokenFactory.generateAccessToken(
                USER_ID,
                "jdoe",
                List.of("OFFICER"),
                List.of("idm:user:read", "idm:user:write", "idm:role:read"),
                "session-threshold");

        assertThat(jwtClaimsExtractor.extractPermissions(token)).isEmpty();
        assertThat(jwtClaimsExtractor.extractRoles(token)).containsExactly("OFFICER");
    }
}
