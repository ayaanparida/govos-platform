package com.govos.security.service;

import java.util.UUID;

/**
 * Logout orchestration. Implementation deferred to Phase 2.
 */
public interface LogoutService {

    void logout(String refreshToken);

    void logoutAll(UUID userId);
}
