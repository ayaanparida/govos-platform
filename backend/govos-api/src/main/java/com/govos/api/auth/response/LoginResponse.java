package com.govos.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Successful login or refresh payload with issued tokens")
public record LoginResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Opaque refresh token for session renewal", example = "f47ac10b-58cc-4372-a567-0e02b2c3d479")
        String refreshToken,
        @Schema(description = "Access token lifetime in seconds", example = "900")
        long expiresIn,
        @Schema(description = "Token type", example = "Bearer")
        String tokenType,
        @Schema(description = "Authenticated user summary")
        AuthUserResponse user
) {
}
