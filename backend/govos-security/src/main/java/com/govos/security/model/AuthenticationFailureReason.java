package com.govos.security.model;

/**
 * Structured failure reasons returned from {@link com.govos.security.service.AuthenticationService}.
 */
public enum AuthenticationFailureReason {
    USER_NOT_FOUND,
    ACCOUNT_DISABLED,
    ACCOUNT_LOCKED,
    INVALID_PASSWORD,
    PASSWORD_EXPIRED,
    PASSWORD_NOT_INITIALIZED
}
