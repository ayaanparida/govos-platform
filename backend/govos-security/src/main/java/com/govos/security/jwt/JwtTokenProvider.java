package com.govos.security.jwt;

import com.govos.security.provider.GovosUserPrincipal;

/**
 * Issues signed JWT access tokens. Implementation deferred to Phase 2.
 */
public interface JwtTokenProvider {

    String createAccessToken(GovosUserPrincipal principal, String sessionId);
}
