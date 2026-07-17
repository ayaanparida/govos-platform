package com.govos.security.model;

import com.govos.security.provider.GovosUserPrincipal;

import java.util.UUID;

public record AuthenticationResult(
        boolean success,
        GovosUserPrincipal principal,
        AuthenticationFailureReason failureReason,
        UUID loginHistoryId,
        UUID auditSessionId,
        String sessionId
) {

    public static AuthenticationResult success(
            GovosUserPrincipal principal,
            UUID loginHistoryId,
            UUID auditSessionId,
            String sessionId) {
        return new AuthenticationResult(
                true, principal, null, loginHistoryId, auditSessionId, sessionId);
    }

    public static AuthenticationResult failure(AuthenticationFailureReason reason) {
        return new AuthenticationResult(false, null, reason, null, null, null);
    }
}
