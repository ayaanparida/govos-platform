package com.govos.security.constant;

/**
 * Shared security constants for JWT claims, authority prefixes, and configuration keys.
 */
public final class SecurityConstants {

    public static final String CONFIG_PREFIX = "govos.security";

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String JWT_CLAIM_USERNAME = "username";
    public static final String JWT_CLAIM_ORG = "org";
    public static final String JWT_CLAIM_ROLES = "roles";
    public static final String JWT_CLAIM_PERMISSIONS = "permissions";
    public static final String JWT_CLAIM_SESSION_ID = "session_id";
    public static final String JWT_CLAIM_JTI = "jti";

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String REQUEST_ID_ATTRIBUTE = "govos.requestId";
    public static final String AUTHENTICATED_USERNAME_ATTRIBUTE = "govos.security.username";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private SecurityConstants() {
    }
}
