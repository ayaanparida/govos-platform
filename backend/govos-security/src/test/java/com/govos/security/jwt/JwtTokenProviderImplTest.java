package com.govos.security.jwt;

import com.govos.security.config.SecurityConfigurationProperties;
import com.govos.security.config.SecurityProperties;
import com.govos.security.provider.GovosUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderImplTest {

    private static final String TEST_SECRET =
            "govos-test-jwt-secret-key-minimum-sixty-four-bytes-long-for-hs512-signing!!";

    private SecurityProperties securityProperties;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        SecurityConfigurationProperties.Jwt jwt = new SecurityConfigurationProperties.Jwt();
        jwt.setSecret(TEST_SECRET);
        jwt.setIssuer("govos-test");
        jwt.setAccessTokenTtl(Duration.ofMinutes(15));
        securityProperties.setJwt(jwt);

        JwtTokenFactory jwtTokenFactory = new JwtTokenFactory(securityProperties);
        jwtTokenProvider = new JwtTokenProviderImpl(jwtTokenFactory);
    }

    @Test
    void shouldGenerateTokenFromGovosUserPrincipal() {
        GovosUserPrincipal principal = new GovosUserPrincipal(
                UUID.randomUUID(),
                "jdoe",
                "",
                "john@example.com",
                List.of(
                        new SimpleGrantedAuthority("ROLE_OFFICER"),
                        new SimpleGrantedAuthority("idm:user:read")),
                true,
                true);

        String token = jwtTokenProvider.createAccessToken(principal, "session-abc");

        assertThat(token).isNotBlank();
        assertThat(new JwtTokenValidatorImpl(new JwtTokenFactory(securityProperties)).validateAccessToken(token))
                .isTrue();
    }
}
