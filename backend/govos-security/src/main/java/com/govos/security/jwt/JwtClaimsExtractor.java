package com.govos.security.jwt;

import java.util.List;
import java.util.UUID;

/**
 * Extracts claims from validated JWT access tokens. Implementation deferred to Phase 2.
 */
public interface JwtClaimsExtractor {

    UUID extractUserId(String token);

    String extractUsername(String token);

    String extractSessionId(String token);

    List<String> extractRoles(String token);

    List<String> extractPermissions(String token);
}
