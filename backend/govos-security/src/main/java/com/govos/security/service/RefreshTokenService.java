package com.govos.security.service;

import java.util.UUID;

/**
 * Refresh-token lifecycle orchestration for the security layer.
 * <p>
 * Distinct from {@code com.govos.idm.service.RefreshTokenService}, which owns persistence.
 */
public interface RefreshTokenService {

    void createRefreshToken(UUID userId);

    void revokeRefreshToken(String refreshToken);

    void revokeAllForUser(UUID userId);
}
