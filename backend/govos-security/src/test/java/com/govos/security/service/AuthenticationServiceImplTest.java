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
import com.govos.security.config.SecurityConfigurationProperties;
import com.govos.security.config.SecurityProperties;
import com.govos.security.model.AuthenticationFailureReason;
import com.govos.security.model.AuthenticationRequest;
import com.govos.security.model.AuthenticationResult;
import com.govos.security.password.PasswordCredentialResolver;
import com.govos.security.password.PasswordPolicyValidator;
import com.govos.security.provider.GovosUserPrincipal;
import com.govos.security.provider.GovosUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private LoginHistoryService loginHistoryService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    private PasswordCredentialResolver passwordCredentialResolver;

    @Mock
    private PasswordPolicyValidator passwordPolicyValidator;

    @Mock
    private GovosUserDetailsService govosUserDetailsService;

    @Mock
    private SecurityAuditPublisher securityAuditPublisher;

    private AuthenticationServiceImpl authenticationService;
    private SecurityProperties securityProperties;
    private UUID userId;
    private UserDto activeUser;
    private PasswordHistoryDto passwordHistory;
    private AuthenticationRequest request;

    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        SecurityConfigurationProperties.Lockout lockout = new SecurityConfigurationProperties.Lockout();
        lockout.setMaxAttempts(3);
        securityProperties.setLockout(lockout);

        authenticationService = new AuthenticationServiceImpl(
                userService,
                loginHistoryService,
                passwordEncoder,
                passwordCredentialResolver,
                passwordPolicyValidator,
                govosUserDetailsService,
                securityAuditPublisher,
                securityProperties);

        userId = UUID.randomUUID();
        activeUser = userDto(userId, "jdoe", UserStatus.ACTIVE, false, 0);
        passwordHistory = new PasswordHistoryDto(
                UUID.randomUUID(),
                "PWD-001",
                userId,
                "$2a$12$hash",
                Instant.now().minusSeconds(3600),
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());
        request = new AuthenticationRequest(
                "jdoe",
                "Secret123!",
                "127.0.0.1",
                "desktop",
                "chrome",
                "Mozilla/5.0");
    }

    @Test
    void shouldFailWhenUserNotFound() {
        when(userService.getByUsername("jdoe")).thenThrow(new UserNotFoundException("jdoe"));

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.success()).isFalse();
        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.USER_NOT_FOUND);
        verify(loginHistoryService, never()).record(any());
    }

    @Test
    void shouldFailWhenAccountDisabled() {
        UserDto disabled = userDto(userId, "jdoe", UserStatus.INACTIVE, false, 0);
        when(userService.getByUsername("jdoe")).thenReturn(disabled);

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.ACCOUNT_DISABLED);
        verify(loginHistoryService).record(any(CreateLoginHistoryRequest.class));
    }

    @Test
    void shouldFailWhenAccountLocked() {
        UserDto locked = userDto(userId, "jdoe", UserStatus.ACTIVE, true, 0);
        when(userService.getByUsername("jdoe")).thenReturn(locked);

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.ACCOUNT_LOCKED);
    }

    @Test
    void shouldFailWhenPasswordNotInitialized() {
        when(userService.getByUsername("jdoe")).thenReturn(activeUser);
        when(passwordCredentialResolver.resolveLatest(userId)).thenReturn(Optional.empty());

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.PASSWORD_NOT_INITIALIZED);
    }

    @Test
    void shouldFailWhenPasswordExpired() {
        when(userService.getByUsername("jdoe")).thenReturn(activeUser);
        when(passwordCredentialResolver.resolveLatest(userId)).thenReturn(Optional.of(passwordHistory));
        when(passwordPolicyValidator.isExpired(eq(userId), any(Instant.class))).thenReturn(true);

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.PASSWORD_EXPIRED);
    }

    @Test
    void shouldFailWhenPasswordInvalid() {
        when(userService.getByUsername("jdoe")).thenReturn(activeUser);
        when(passwordCredentialResolver.resolveLatest(userId)).thenReturn(Optional.of(passwordHistory));
        when(passwordPolicyValidator.isExpired(eq(userId), any(Instant.class))).thenReturn(false);
        when(passwordPolicyValidator.requireCurrentCredentials(userId)).thenReturn(passwordHistory);
        when(passwordEncoder.matches("Secret123!", "$2a$12$hash")).thenReturn(false);

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.INVALID_PASSWORD);
        verify(userService).recordFailedLogin(userId);
        verify(userService, never()).lockAccount(userId);
    }

    @Test
    void shouldLockAccountWhenThresholdReached() {
        UserDto user = userDto(userId, "jdoe", UserStatus.ACTIVE, false, 2);
        when(userService.getByUsername("jdoe")).thenReturn(user);
        when(passwordCredentialResolver.resolveLatest(userId)).thenReturn(Optional.of(passwordHistory));
        when(passwordPolicyValidator.isExpired(eq(userId), any(Instant.class))).thenReturn(false);
        when(passwordPolicyValidator.requireCurrentCredentials(userId)).thenReturn(passwordHistory);
        when(passwordEncoder.matches("Secret123!", "$2a$12$hash")).thenReturn(false);

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.failureReason()).isEqualTo(AuthenticationFailureReason.ACCOUNT_LOCKED);
        verify(userService).lockAccount(userId);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        UUID loginHistoryId = UUID.randomUUID();
        UUID auditSessionId = UUID.randomUUID();
        GovosUserPrincipal principal = new GovosUserPrincipal(
                userId,
                "jdoe",
                "",
                "john@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_OFFICER")),
                true,
                true);

        when(userService.getByUsername("jdoe")).thenReturn(activeUser);
        when(passwordCredentialResolver.resolveLatest(userId)).thenReturn(Optional.of(passwordHistory));
        when(passwordPolicyValidator.isExpired(eq(userId), any(Instant.class))).thenReturn(false);
        when(passwordPolicyValidator.requireCurrentCredentials(userId)).thenReturn(passwordHistory);
        when(passwordEncoder.matches("Secret123!", "$2a$12$hash")).thenReturn(true);
        when(loginHistoryService.record(any(CreateLoginHistoryRequest.class))).thenReturn(
                loginHistoryDto(loginHistoryId));
        when(securityAuditPublisher.openSession(any(), any(), any(), any(), any())).thenReturn(
                auditSessionDto(auditSessionId));
        when(govosUserDetailsService.loadUserByUsername("jdoe")).thenReturn(principal);

        AuthenticationResult result = authenticationService.authenticate(request);

        assertThat(result.success()).isTrue();
        assertThat(result.principal()).isEqualTo(principal);
        assertThat(result.loginHistoryId()).isEqualTo(loginHistoryId);
        assertThat(result.auditSessionId()).isEqualTo(auditSessionId);
        assertThat(result.sessionId()).isNotBlank();

        verify(userService).resetFailedLoginAttempts(userId);
        verify(userService).updateLastLogin(eq(userId), any(Instant.class));

        ArgumentCaptor<CreateLoginHistoryRequest> captor = ArgumentCaptor.forClass(CreateLoginHistoryRequest.class);
        verify(loginHistoryService).record(captor.capture());
        assertThat(captor.getValue().success()).isTrue();
    }

    private UserDto userDto(
            UUID id,
            String username,
            UserStatus status,
            boolean locked,
            int failedAttempts) {
        return new UserDto(
                id,
                "USR-001",
                username,
                "john@example.com",
                null,
                "John",
                null,
                "Doe",
                null,
                LocalDate.of(1990, 1, 1),
                status,
                locked,
                failedAttempts,
                null,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }

    private LoginHistoryDto loginHistoryDto(UUID id) {
        return new LoginHistoryDto(
                id,
                "LH-001",
                userId,
                Instant.now(),
                null,
                "127.0.0.1",
                "desktop",
                "chrome",
                true,
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }

    private AuditSessionDto auditSessionDto(UUID id) {
        return new AuditSessionDto(
                id,
                "SES-001",
                "session-id",
                Instant.now(),
                null,
                "127.0.0.1",
                "desktop",
                "chrome",
                true,
                0L,
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
