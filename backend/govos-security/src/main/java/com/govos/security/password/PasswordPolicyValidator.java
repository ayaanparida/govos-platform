package com.govos.security.password;

import com.govos.idm.dto.PasswordHistoryDto;
import com.govos.security.config.SecurityProperties;
import com.govos.security.exception.PasswordPolicyException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Validates password lifecycle policy using IDM password history.
 */
@Component
public class PasswordPolicyValidator {

    private final PasswordCredentialResolver passwordCredentialResolver;
    private final SecurityProperties securityProperties;

    public PasswordPolicyValidator(
            PasswordCredentialResolver passwordCredentialResolver,
            SecurityProperties securityProperties) {
        this.passwordCredentialResolver = passwordCredentialResolver;
        this.securityProperties = securityProperties;
    }

    public PasswordHistoryDto requireCurrentCredentials(UUID userId) {
        return passwordCredentialResolver.resolveLatest(userId)
                .orElseThrow(() -> new PasswordPolicyException(
                        "No password history found for user: " + userId));
    }

    public void validateNotExpired(UUID userId, Instant referenceTime) {
        PasswordHistoryDto latest = requireCurrentCredentials(userId);
        Instant expiresAt = latest.changedDate().plus(securityProperties.getPassword().getMaxAge());
        if (!expiresAt.isAfter(referenceTime)) {
            throw new PasswordPolicyException("Password expired for user: " + userId);
        }
    }

    public boolean isExpired(UUID userId, Instant referenceTime) {
        return passwordCredentialResolver.resolveLatest(userId)
                .map(latest -> !latest.changedDate()
                        .plus(securityProperties.getPassword().getMaxAge())
                        .isAfter(referenceTime))
                .orElse(true);
    }
}
