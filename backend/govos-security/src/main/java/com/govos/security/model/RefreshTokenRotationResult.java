package com.govos.security.model;

import java.util.UUID;

public record RefreshTokenRotationResult(
        UUID userId,
        String refreshToken,
        String sessionId
) {
}
