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

    private SecurityConstants() {
    }
}
