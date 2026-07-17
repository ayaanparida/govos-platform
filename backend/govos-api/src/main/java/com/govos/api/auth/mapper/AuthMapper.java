package com.govos.api.auth.mapper;

import com.govos.api.auth.request.LoginRequest;
import com.govos.api.auth.response.CurrentUserResponse;
import com.govos.api.auth.response.LoginResponse;
import com.govos.security.model.AuthenticationRequest;
import com.govos.security.model.AuthenticationResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Maps between auth REST contracts and {@code govos-security} service models.
 */
@Component
public class AuthMapper {

    public AuthenticationRequest toAuthenticationRequest(LoginRequest request, HttpServletRequest httpRequest) {
        return new AuthenticationRequest(
                request.username(),
                request.password(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("X-Device"),
                httpRequest.getHeader("X-Browser"),
                httpRequest.getHeader("User-Agent"));
    }

    public LoginResponse toLoginResponse(AuthenticationResult result, String accessToken, String refreshToken, long expiresIn) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn,
                result.sessionId() != null ? java.util.UUID.fromString(result.sessionId()) : null);
    }

    public CurrentUserResponse placeholderCurrentUser() {
        return new CurrentUserResponse(
                false,
                null,
                null,
                null,
                emptyList(),
                emptyList());
    }

    private static List<String> emptyList() {
        return Collections.emptyList();
    }
}
