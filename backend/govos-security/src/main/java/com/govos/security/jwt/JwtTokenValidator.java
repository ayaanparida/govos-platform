package com.govos.security.jwt;

/**
 * Validates JWT access tokens. Implementation deferred to Phase 2.
 */
public interface JwtTokenValidator {

    boolean validateAccessToken(String token);
}
