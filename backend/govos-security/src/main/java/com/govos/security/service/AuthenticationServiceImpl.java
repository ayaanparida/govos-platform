package com.govos.security.service;

import com.govos.audit.dto.AuditSessionDto;
import com.govos.idm.dto.CreateLoginHistoryRequest;
import com.govos.idm.dto.LoginHistoryDto;
import com.govos.idm.dto.PasswordHistoryDto;
import com.govos.idm.dto.UserDto;
import com.govos.idm.entity.UserStatus;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.service.LoginHistoryService;
import com.govos.idm.service.UserService;
import com.govos.security.audit.SecurityAuditPublisher;
import com.govos.security.config.SecurityProperties;
import com.govos.security.model.AuthenticationFailureReason;
import com.govos.security.model.AuthenticationRequest;
import com.govos.security.model.AuthenticationResult;
import com.govos.security.password.PasswordCredentialResolver;
import com.govos.security.password.PasswordPolicyValidator;
import com.govos.security.provider.GovosUserPrincipal;
import com.govos.security.provider.GovosUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private final LoginHistoryService loginHistoryService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordCredentialResolver passwordCredentialResolver;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final GovosUserDetailsService govosUserDetailsService;
    private final SecurityAuditPublisher securityAuditPublisher;
    private final SecurityProperties securityProperties;

    public AuthenticationServiceImpl(
            UserService userService,
            LoginHistoryService loginHistoryService,
            PasswordEncoder passwordEncoder,
            PasswordCredentialResolver passwordCredentialResolver,
            PasswordPolicyValidator passwordPolicyValidator,
            GovosUserDetailsService govosUserDetailsService,
            SecurityAuditPublisher securityAuditPublisher,
            SecurityProperties securityProperties) {
        this.userService = userService;
        this.loginHistoryService = loginHistoryService;
        this.passwordEncoder = passwordEncoder;
        this.passwordCredentialResolver = passwordCredentialResolver;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.govosUserDetailsService = govosUserDetailsService;
        this.securityAuditPublisher = securityAuditPublisher;
        this.securityProperties = securityProperties;
    }

    @Override
    @Transactional
    public AuthenticationResult authenticate(AuthenticationRequest request) {
        Instant now = Instant.now();

        UserDto user;
        try {
            user = userService.getByUsername(request.username());
        } catch (UserNotFoundException ex) {
            securityAuditPublisher.publishLoginFailure(
                    null,
                    null,
                    AuthenticationFailureReason.USER_NOT_FOUND,
                    request.ipAddress(),
                    request.userAgent(),
                    now);
            return AuthenticationResult.failure(AuthenticationFailureReason.USER_NOT_FOUND);
        }

        if (!isAccountEnabled(user)) {
            recordFailedAttempt(user, request, now, AuthenticationFailureReason.ACCOUNT_DISABLED);
            return AuthenticationResult.failure(AuthenticationFailureReason.ACCOUNT_DISABLED);
        }

        if (Boolean.TRUE.equals(user.accountLocked())) {
            recordFailedAttempt(user, request, now, AuthenticationFailureReason.ACCOUNT_LOCKED);
            return AuthenticationResult.failure(AuthenticationFailureReason.ACCOUNT_LOCKED);
        }

        if (passwordCredentialResolver.resolveLatest(user.id()).isEmpty()) {
            recordFailedAttempt(user, request, now, AuthenticationFailureReason.PASSWORD_NOT_INITIALIZED);
            return AuthenticationResult.failure(AuthenticationFailureReason.PASSWORD_NOT_INITIALIZED);
        }

        if (passwordPolicyValidator.isExpired(user.id(), now)) {
            recordFailedAttempt(user, request, now, AuthenticationFailureReason.PASSWORD_EXPIRED);
            return AuthenticationResult.failure(AuthenticationFailureReason.PASSWORD_EXPIRED);
        }

        PasswordHistoryDto credentials = passwordPolicyValidator.requireCurrentCredentials(user.id());
        if (!passwordEncoder.matches(request.password(), credentials.passwordHash())) {
            return handleInvalidPassword(user, request, now);
        }

        return handleSuccessfulLogin(user, request, now);
    }

    private AuthenticationResult handleSuccessfulLogin(
            UserDto user,
            AuthenticationRequest request,
            Instant now) {
        userService.resetFailedLoginAttempts(user.id());
        userService.updateLastLogin(user.id(), now);

        LoginHistoryDto loginHistory = loginHistoryService.record(new CreateLoginHistoryRequest(
                user.id(),
                now,
                null,
                request.ipAddress(),
                request.device(),
                request.browser(),
                true,
                true));

        String sessionId = UUID.randomUUID().toString();
        AuditSessionDto auditSession = securityAuditPublisher.openSession(
                sessionId,
                now,
                request.ipAddress(),
                request.device(),
                request.browser());

        securityAuditPublisher.publishLoginSuccess(
                user.id(),
                auditSession.id(),
                request.ipAddress(),
                request.userAgent(),
                now);

        GovosUserPrincipal principal = (GovosUserPrincipal) govosUserDetailsService
                .loadUserByUsername(user.username());

        return AuthenticationResult.success(
                principal,
                loginHistory.id(),
                auditSession.id(),
                sessionId);
    }

    private AuthenticationResult handleInvalidPassword(
            UserDto user,
            AuthenticationRequest request,
            Instant now) {
        int currentAttempts = user.failedLoginAttempts() == null ? 0 : user.failedLoginAttempts();
        int nextAttempts = currentAttempts + 1;

        userService.recordFailedLogin(user.id());

        AuthenticationFailureReason reason = AuthenticationFailureReason.INVALID_PASSWORD;
        if (nextAttempts >= securityProperties.getLockout().getMaxAttempts()) {
            userService.lockAccount(user.id());
            reason = AuthenticationFailureReason.ACCOUNT_LOCKED;
        }

        recordFailedAttempt(user, request, now, reason);
        return AuthenticationResult.failure(reason);
    }

    private void recordFailedAttempt(
            UserDto user,
            AuthenticationRequest request,
            Instant now,
            AuthenticationFailureReason reason) {
        loginHistoryService.record(new CreateLoginHistoryRequest(
                user.id(),
                now,
                null,
                request.ipAddress(),
                request.device(),
                request.browser(),
                false,
                true));

        securityAuditPublisher.publishLoginFailure(
                user.id(),
                null,
                reason,
                request.ipAddress(),
                request.userAgent(),
                now);
    }

    private boolean isAccountEnabled(UserDto user) {
        return Boolean.TRUE.equals(user.active()) && user.status() == UserStatus.ACTIVE;
    }
}
