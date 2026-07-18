package com.govos.security.password;

import com.govos.idm.dto.PasswordHistoryDto;
import com.govos.idm.service.PasswordHistoryService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves the current password hash from IDM password history (most recent entry).
 */
@Component
public class PasswordCredentialResolver {

    private final PasswordHistoryService passwordHistoryService;

    public PasswordCredentialResolver(PasswordHistoryService passwordHistoryService) {
        this.passwordHistoryService = passwordHistoryService;
    }

    public Optional<PasswordHistoryDto> resolveLatest(UUID userId) {
        List<PasswordHistoryDto> history = passwordHistoryService.getByUserId(userId);
        if (history.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(history.getFirst());
    }
}
