package com.govos.security.service;

import com.govos.security.model.RefreshTokenRotationResult;

import java.util.UUID;

/**
 * Refresh-token lifecycle orchestration for the security layer.
 * <p>
 * Distinct from {@code com.govos.idm.service.RefreshTokenService}, which owns persistence.
 */
public interface RefreshTokenService {

    String createRefreshToken(UUID userId);

    RefreshTokenRotationResult rotateRefreshToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);

    void revokeAllForUser(UUID userId);
}
