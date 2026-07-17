package com.govos.security.service;

import com.govos.idm.dto.LoginHistoryDto;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.service.LoginHistoryService;
import com.govos.security.audit.SecurityAuditPublisher;
import com.govos.security.util.RefreshTokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceImplTest {

    @Mock
    private com.govos.idm.service.RefreshTokenService idmRefreshTokenService;

    @Mock
    private LoginHistoryService loginHistoryService;

    @Mock
    private SecurityAuditPublisher securityAuditPublisher;

    private LogoutServiceImpl logoutService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        logoutService = new LogoutServiceImpl(
                idmRefreshTokenService,
                loginHistoryService,
                securityAuditPublisher);
        userId = UUID.randomUUID();
    }

    @Test
    void shouldLogoutSingleRefreshToken() {
        String rawToken = "refresh-token-value";
        String hashedToken = RefreshTokenHasher.hash(rawToken);
        RefreshTokenDto token = new RefreshTokenDto(
                UUID.randomUUID(),
                "RT-001",
                userId,
                hashedToken,
                Instant.now().plusSeconds(3600),
                false,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());

        LoginHistoryDto openSession = new LoginHistoryDto(
                UUID.randomUUID(),
                "LH-001",
                userId,
                Instant.now().minusSeconds(600),
                null,
                "127.0.0.1",
                "desktop",
                "chrome",
                true,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());

        when(idmRefreshTokenService.getByToken(hashedToken)).thenReturn(token);
        when(loginHistoryService.getByUserId(userId)).thenReturn(List.of(openSession));

        logoutService.logout(rawToken);

        verify(idmRefreshTokenService).revokeByToken(hashedToken);
        verify(loginHistoryService).record(any());
        verify(securityAuditPublisher).publishLogout(eq(userId), isNull(), isNull(), isNull(), any(Instant.class));
    }

    @Test
    void shouldLogoutAllSessionsForUser() {
        RefreshTokenDto first = refreshToken(UUID.randomUUID());
        RefreshTokenDto second = refreshToken(UUID.randomUUID());

        when(idmRefreshTokenService.getActiveByUserId(userId)).thenReturn(List.of(first, second));
        when(loginHistoryService.getByUserId(userId)).thenReturn(List.of());

        logoutService.logoutAll(userId);

        verify(idmRefreshTokenService).revoke(first.id());
        verify(idmRefreshTokenService).revoke(second.id());
        verify(securityAuditPublisher).publishLogout(eq(userId), isNull(), isNull(), isNull(), any(Instant.class));
    }

    private RefreshTokenDto refreshToken(UUID id) {
        return new RefreshTokenDto(
                id,
                "RT-" + id,
                userId,
                "hashed",
                Instant.now().plusSeconds(3600),
                false,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
