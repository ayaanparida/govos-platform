package com.govos.security.jwt;

/**
 * JWT-specific constants for GovOS access tokens.
 */
public final class JwtConstants {

    public static final String ALGORITHM = "HS512";
    public static final String TOKEN_TYPE = "Bearer";

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_PERMISSIONS = "permissions";
    public static final String CLAIM_SESSION_ID = "session_id";

    public static final int MIN_SECRET_BYTES_HS512 = 64;

    private JwtConstants() {
    }
}
