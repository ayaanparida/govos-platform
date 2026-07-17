package com.govos.security.service;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.security.config.SecurityProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import com.govos.security.util.RefreshTokenHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service("securityRefreshTokenService")
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final com.govos.idm.service.RefreshTokenService idmRefreshTokenService;
    private final SecurityProperties securityProperties;

    public RefreshTokenServiceImpl(
            @Qualifier("refreshTokenServiceImpl") com.govos.idm.service.RefreshTokenService idmRefreshTokenService,
            SecurityProperties securityProperties) {
        this.idmRefreshTokenService = idmRefreshTokenService;
        this.securityProperties = securityProperties;
    }

    @Override
    @Transactional
    public void createRefreshToken(UUID userId) {
        enforceSessionLimit(userId);

        String rawToken = UUID.randomUUID().toString();
        String hashedToken = RefreshTokenHasher.hash(rawToken);
        Instant expiry = Instant.now().plus(securityProperties.getJwt().getRefreshTokenTtl());

        idmRefreshTokenService.create(new CreateRefreshTokenRequest(
                userId,
                hashedToken,
                expiry,
                false,
                true));
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        idmRefreshTokenService.revokeByToken(RefreshTokenHasher.hash(refreshToken));
    }

    @Override
    @Transactional
    public void revokeAllForUser(UUID userId) {
        List<RefreshTokenDto> activeTokens = idmRefreshTokenService.getActiveByUserId(userId);
        for (RefreshTokenDto token : activeTokens) {
            idmRefreshTokenService.revoke(token.id());
        }
    }

    private void enforceSessionLimit(UUID userId) {
        int maxPerUser = securityProperties.getSession().getMaxPerUser();
        List<RefreshTokenDto> activeTokens = idmRefreshTokenService.getActiveByUserId(userId);

        if (activeTokens.size() < maxPerUser) {
            return;
        }

        activeTokens.stream()
                .min(Comparator.comparing(RefreshTokenDto::createdDate))
                .ifPresent(oldest -> idmRefreshTokenService.revoke(oldest.id()));
    }
}
