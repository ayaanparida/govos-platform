package com.govos.api.auth.mapper;

import com.govos.api.auth.request.LoginRequest;
import com.govos.api.auth.response.AuthUserResponse;
import com.govos.api.auth.response.CurrentUserResponse;
import com.govos.api.auth.response.LoginResponse;
import com.govos.api.auth.response.LogoutResponse;
import com.govos.security.constant.SecurityConstants;
import com.govos.security.model.AuthenticationRequest;
import com.govos.security.provider.GovosUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public LoginResponse toLoginResponse(
            GovosUserPrincipal principal,
            String accessToken,
            String refreshToken,
            long expiresIn) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                expiresIn,
                "Bearer",
                toAuthUserResponse(principal));
    }

    public CurrentUserResponse toCurrentUserResponse(GovosUserPrincipal principal) {
        AuthoritySplit authorities = splitAuthorities(principal.getAuthorities());
        return new CurrentUserResponse(
                true,
                principal.getUserId(),
                principal.getUsername(),
                principal.getEmail(),
                authorities.roles(),
                authorities.permissions());
    }

    public LogoutResponse toLogoutResponse() {
        return new LogoutResponse("Session revoked successfully");
    }

    private AuthUserResponse toAuthUserResponse(GovosUserPrincipal principal) {
        AuthoritySplit authorities = splitAuthorities(principal.getAuthorities());
        return new AuthUserResponse(
                principal.getUserId(),
                principal.getUsername(),
                principal.getEmail(),
                authorities.roles(),
                authorities.permissions());
    }

    private AuthoritySplit splitAuthorities(Iterable<? extends GrantedAuthority> authorities) {
        List<String> roles = new ArrayList<>();
        List<String> permissions = new ArrayList<>();

        for (GrantedAuthority authority : authorities) {
            String value = authority.getAuthority();
            if (value.startsWith(SecurityConstants.ROLE_PREFIX)) {
                roles.add(value.substring(SecurityConstants.ROLE_PREFIX.length()));
            } else {
                permissions.add(value);
            }
        }

        return new AuthoritySplit(List.copyOf(roles), List.copyOf(permissions));
    }

    private record AuthoritySplit(List<String> roles, List<String> permissions) {
    }
}
