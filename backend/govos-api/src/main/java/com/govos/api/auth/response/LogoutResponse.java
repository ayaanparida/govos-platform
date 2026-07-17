package com.govos.api.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Logout confirmation payload")
public record LogoutResponse(
        @Schema(description = "Logout status message", example = "Session revoked successfully")
        String message
) {
}
