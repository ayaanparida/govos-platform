package com.govos.security.service;

import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.service.LoginHistoryService;
import com.govos.security.audit.SecurityAuditPublisher;
import com.govos.security.util.LoginHistoryLogoutSupport;
import com.govos.security.util.RefreshTokenHasher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class LogoutServiceImpl implements LogoutService {

    private final com.govos.idm.service.RefreshTokenService idmRefreshTokenService;
    private final LoginHistoryService loginHistoryService;
    private final SecurityAuditPublisher securityAuditPublisher;

    public LogoutServiceImpl(
            @Qualifier("refreshTokenServiceImpl") com.govos.idm.service.RefreshTokenService idmRefreshTokenService,
            LoginHistoryService loginHistoryService,
            SecurityAuditPublisher securityAuditPublisher) {
        this.idmRefreshTokenService = idmRefreshTokenService;
        this.loginHistoryService = loginHistoryService;
        this.securityAuditPublisher = securityAuditPublisher;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        RefreshTokenDto token = idmRefreshTokenService.getByToken(RefreshTokenHasher.hash(refreshToken));
        UUID userId = token.userId();
        Instant now = Instant.now();

        idmRefreshTokenService.revokeByToken(RefreshTokenHasher.hash(refreshToken));
        LoginHistoryLogoutSupport.closeOpenSession(
                loginHistoryService,
                userId,
                now,
                null,
                null,
                null);
        securityAuditPublisher.publishLogout(userId, null, null, null, now);
    }

    @Override
    @Transactional
    public void logoutAll(UUID userId) {
        Instant now = Instant.now();

        idmRefreshTokenService.getActiveByUserId(userId).forEach(activeToken ->
                idmRefreshTokenService.revoke(activeToken.id()));

        LoginHistoryLogoutSupport.closeOpenSession(
                loginHistoryService,
                userId,
                now,
                null,
                null,
                null);
        securityAuditPublisher.publishLogout(userId, null, null, null, now);
    }
}
