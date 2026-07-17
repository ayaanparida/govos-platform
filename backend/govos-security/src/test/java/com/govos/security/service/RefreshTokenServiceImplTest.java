package com.govos.security.service;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.exception.RefreshTokenNotFoundException;
import com.govos.security.config.SecurityConfigurationProperties;
import com.govos.security.config.SecurityProperties;
import com.govos.security.exception.InvalidTokenException;
import com.govos.security.model.RefreshTokenRotationResult;
import com.govos.security.util.RefreshTokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private com.govos.idm.service.RefreshTokenService idmRefreshTokenService;

    private RefreshTokenServiceImpl refreshTokenService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        SecurityProperties securityProperties = new SecurityProperties();
        SecurityConfigurationProperties.Jwt jwt = new SecurityConfigurationProperties.Jwt();
        jwt.setRefreshTokenTtl(Duration.ofDays(7));
        securityProperties.setJwt(jwt);

        SecurityConfigurationProperties.Session session = new SecurityConfigurationProperties.Session();
        session.setMaxPerUser(2);
        securityProperties.setSession(session);

        refreshTokenService = new RefreshTokenServiceImpl(idmRefreshTokenService, securityProperties);
        userId = UUID.randomUUID();
    }

    @Test
    void shouldCreateRefreshTokenMetadata() {
        when(idmRefreshTokenService.getActiveByUserId(userId)).thenReturn(List.of());

        String rawToken = refreshTokenService.createRefreshToken(userId);

        ArgumentCaptor<CreateRefreshTokenRequest> captor = ArgumentCaptor.forClass(CreateRefreshTokenRequest.class);
        verify(idmRefreshTokenService).create(captor.capture());

        CreateRefreshTokenRequest request = captor.getValue();
        assertThat(rawToken).isNotBlank();
        assertThat(UUID.fromString(rawToken)).isNotNull();
        assertThat(request.userId()).isEqualTo(userId);
        assertThat(request.revoked()).isFalse();
        assertThat(request.active()).isTrue();
        assertThat(request.expiry()).isAfter(Instant.now());
        assertThat(request.token()).isEqualTo(RefreshTokenHasher.hash(rawToken));
    }

    @Test
    void shouldRevokeOldestTokenWhenSessionLimitReached() {
        RefreshTokenDto oldest = refreshToken(UUID.randomUUID(), Instant.now().minusSeconds(7200));
        RefreshTokenDto newest = refreshToken(UUID.randomUUID(), Instant.now().minusSeconds(3600));

        when(idmRefreshTokenService.getActiveByUserId(userId)).thenReturn(List.of(newest, oldest));

        refreshTokenService.createRefreshToken(userId);

        verify(idmRefreshTokenService).revoke(oldest.id());
        verify(idmRefreshTokenService).create(any(CreateRefreshTokenRequest.class));
    }

    @Test
    void shouldRotateRefreshToken() {
        String rawToken = "f47ac10b-58cc-4372-a567-0e02b2c3d479";
        String hashedToken = RefreshTokenHasher.hash(rawToken);
        RefreshTokenDto existing = refreshToken(UUID.randomUUID(), Instant.now());

        when(idmRefreshTokenService.getByToken(hashedToken)).thenReturn(existing);
        when(idmRefreshTokenService.getActiveByUserId(userId)).thenReturn(List.of(existing));

        RefreshTokenRotationResult result = refreshTokenService.rotateRefreshToken(rawToken);

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.sessionId()).isNotBlank();
        verify(idmRefreshTokenService).revokeByToken(hashedToken);
        verify(idmRefreshTokenService).create(any(CreateRefreshTokenRequest.class));
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(idmRefreshTokenService.getByToken(any()))
                .thenThrow(new RefreshTokenNotFoundException("missing"));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("missing-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void shouldRevokeRefreshTokenByHashedValue() {
        String rawToken = "raw-refresh-token";

        refreshTokenService.revokeRefreshToken(rawToken);

        verify(idmRefreshTokenService).revokeByToken(RefreshTokenHasher.hash(rawToken));
    }

    @Test
    void shouldRevokeAllTokensForUser() {
        RefreshTokenDto first = refreshToken(UUID.randomUUID(), Instant.now());
        RefreshTokenDto second = refreshToken(UUID.randomUUID(), Instant.now());

        when(idmRefreshTokenService.getActiveByUserId(userId)).thenReturn(List.of(first, second));

        refreshTokenService.revokeAllForUser(userId);

        verify(idmRefreshTokenService).revoke(first.id());
        verify(idmRefreshTokenService).revoke(second.id());
    }

    private RefreshTokenDto refreshToken(UUID id, Instant createdDate) {
        return new RefreshTokenDto(
                id,
                "RT-" + id,
                userId,
                "hashed-token",
                Instant.now().plusSeconds(3600),
                false,
                true,
                0L,
                "system",
                createdDate,
                "system",
                createdDate);
    }
}
