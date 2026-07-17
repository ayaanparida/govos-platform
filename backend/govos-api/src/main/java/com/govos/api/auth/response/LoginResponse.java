package com.govos.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Successful login payload with issued tokens")
public record LoginResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Opaque refresh token for session renewal", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        String refreshToken,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        @Schema(description = "Access token lifetime in seconds", example = "900")
        long expiresIn,
        @Schema(description = "Audit session identifier", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        UUID sessionId
) {
}
