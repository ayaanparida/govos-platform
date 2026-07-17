package com.govos.security.service;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.exception.RefreshTokenNotFoundException;
import com.govos.security.config.SecurityProperties;
import com.govos.security.exception.InvalidTokenException;
import com.govos.security.model.RefreshTokenRotationResult;
import com.govos.security.util.RefreshTokenHasher;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public String createRefreshToken(UUID userId) {
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

        return rawToken;
    }

    @Override
    @Transactional
    public RefreshTokenRotationResult rotateRefreshToken(String refreshToken) {
        RefreshTokenDto existing = resolveActiveRefreshToken(refreshToken);
        validateNotExpired(existing, refreshToken);

        idmRefreshTokenService.revokeByToken(RefreshTokenHasher.hash(refreshToken));

        String rotatedToken = createRefreshToken(existing.userId());
        String sessionId = UUID.randomUUID().toString();

        return new RefreshTokenRotationResult(existing.userId(), rotatedToken, sessionId);
    }

    private RefreshTokenDto resolveActiveRefreshToken(String refreshToken) {
        try {
            return idmRefreshTokenService.getByToken(RefreshTokenHasher.hash(refreshToken));
        } catch (RefreshTokenNotFoundException ex) {
            throw new InvalidTokenException("Invalid or expired refresh token", ex);
        }
    }

    private void validateNotExpired(RefreshTokenDto token, String rawRefreshToken) {
        if (Boolean.FALSE.equals(token.active()) || Boolean.TRUE.equals(token.revoked())) {
            throw new InvalidTokenException("Refresh token is no longer active");
        }
        if (token.expiry() != null && Instant.now().isAfter(token.expiry())) {
            idmRefreshTokenService.revokeByToken(RefreshTokenHasher.hash(rawRefreshToken));
            throw new InvalidTokenException("Refresh token has expired");
        }
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
