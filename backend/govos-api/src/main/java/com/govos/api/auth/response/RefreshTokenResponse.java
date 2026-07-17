package com.govos.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "New access token issued from a valid refresh token")
public record RefreshTokenResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        @Schema(description = "Access token lifetime in seconds", example = "900")
        long expiresIn,
        @Schema(description = "Rotated refresh token when rotation is enabled", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
        String refreshToken
) {
}
