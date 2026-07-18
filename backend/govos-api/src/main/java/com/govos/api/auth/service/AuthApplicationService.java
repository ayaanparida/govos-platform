package com.govos.api.auth.service;

import com.govos.api.auth.mapper.AuthMapper;
import com.govos.api.auth.request.LoginRequest;
import com.govos.api.auth.request.LogoutRequest;
import com.govos.api.auth.request.RefreshTokenRequest;
import com.govos.api.auth.response.CurrentUserResponse;
import com.govos.api.auth.response.LoginResponse;
import com.govos.api.auth.response.LogoutResponse;
import com.govos.api.common.exception.BusinessException;
import com.govos.idm.dto.UserDto;
import com.govos.idm.service.UserService;
import com.govos.security.config.SecurityProperties;
import com.govos.security.exception.AccessDeniedException;
import com.govos.security.exception.AuthenticationFailedException;
import com.govos.security.jwt.JwtPrincipal;
import com.govos.security.jwt.JwtTokenProvider;
import com.govos.security.model.AuthenticationFailureReason;
import com.govos.security.model.AuthenticationRequest;
import com.govos.security.model.AuthenticationResult;
import com.govos.security.model.RefreshTokenRotationResult;
import com.govos.security.provider.GovosUserDetailsService;
import com.govos.security.provider.GovosUserPrincipal;
import com.govos.security.service.AuthenticationService;
import com.govos.security.service.LogoutService;
import com.govos.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthApplicationService {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GovosUserDetailsService govosUserDetailsService;
    private final UserService userService;
    private final SecurityProperties securityProperties;
    private final AuthMapper authMapper;

    public AuthApplicationService(
            AuthenticationService authenticationService,
            @Qualifier("securityRefreshTokenService") RefreshTokenService refreshTokenService,
            LogoutService logoutService,
            JwtTokenProvider jwtTokenProvider,
            GovosUserDetailsService govosUserDetailsService,
            UserService userService,
            SecurityProperties securityProperties,
            AuthMapper authMapper) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.logoutService = logoutService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.govosUserDetailsService = govosUserDetailsService;
        this.userService = userService;
        this.securityProperties = securityProperties;
        this.authMapper = authMapper;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        AuthenticationRequest authenticationRequest = authMapper.toAuthenticationRequest(request, httpRequest);
        AuthenticationResult result = authenticationService.authenticate(authenticationRequest);
        if (!result.success()) {
            throw mapAuthenticationFailure(result.failureReason());
        }

        GovosUserPrincipal principal = result.principal();
        String accessToken = jwtTokenProvider.createAccessToken(principal, result.sessionId());
        String refreshToken = refreshTokenService.createRefreshToken(principal.getUserId());

        return authMapper.toLoginResponse(
                principal,
                accessToken,
                refreshToken,
                accessTokenTtlSeconds());
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshTokenRotationResult rotation = refreshTokenService.rotateRefreshToken(request.refreshToken());
        GovosUserPrincipal principal = loadPrincipal(rotation.userId());
        String accessToken = jwtTokenProvider.createAccessToken(principal, rotation.sessionId());

        return authMapper.toLoginResponse(
                principal,
                accessToken,
                rotation.refreshToken(),
                accessTokenTtlSeconds());
    }

    public LogoutResponse logout(LogoutRequest request) {
        logoutService.logout(request.refreshToken());
        return authMapper.toLogoutResponse();
    }

    public CurrentUserResponse currentUser(JwtPrincipal jwtPrincipal) {
        GovosUserPrincipal principal = loadPrincipal(jwtPrincipal.getUserId());
        return authMapper.toCurrentUserResponse(principal);
    }

    private GovosUserPrincipal loadPrincipal(java.util.UUID userId) {
        UserDto user = userService.getById(userId);
        UserDetails userDetails = govosUserDetailsService.loadUserByUsername(user.username());
        return (GovosUserPrincipal) userDetails;
    }

    private long accessTokenTtlSeconds() {
        return securityProperties.getJwt().getAccessTokenTtl().getSeconds();
    }

    private RuntimeException mapAuthenticationFailure(AuthenticationFailureReason reason) {
        return switch (reason) {
            case ACCOUNT_DISABLED -> new AccessDeniedException("Account is disabled");
            case ACCOUNT_LOCKED -> new AccessDeniedException("Account is locked");
            case PASSWORD_EXPIRED -> new BusinessException(
                    "PASSWORD_EXPIRED",
                    "Password has expired and must be changed");
            case PASSWORD_NOT_INITIALIZED -> new BusinessException(
                    "PASSWORD_NOT_INITIALIZED",
                    "Password is not initialized for this account");
            case USER_NOT_FOUND, INVALID_PASSWORD -> new AuthenticationFailedException("Invalid username or password");
        };
    }
}
