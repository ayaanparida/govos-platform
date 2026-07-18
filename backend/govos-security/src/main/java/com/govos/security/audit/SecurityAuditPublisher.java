package com.govos.security.audit;

import com.govos.audit.dto.AuditSessionDto;
import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.dto.CreateAuditSessionRequest;
import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;
import com.govos.audit.service.AuditEventService;
import com.govos.audit.service.AuditSessionService;
import com.govos.security.model.AuthenticationFailureReason;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes security events through AUD domain services — no direct repository access.
 */
@Component
public class SecurityAuditPublisher {

    static final String USER_ENTITY_TYPE = "idm.User";

    private final AuditEventService auditEventService;
    private final AuditSessionService auditSessionService;

    public SecurityAuditPublisher(
            AuditEventService auditEventService,
            AuditSessionService auditSessionService) {
        this.auditEventService = auditEventService;
        this.auditSessionService = auditSessionService;
    }

    public AuditSessionDto openSession(
            String sessionId,
            Instant loginTime,
            String ipAddress,
            String device,
            String browser) {
        return auditSessionService.create(new CreateAuditSessionRequest(
                null,
                sessionId,
                loginTime,
                null,
                ipAddress,
                device,
                browser,
                true));
    }

    public void publishLoginSuccess(
            UUID userId,
            UUID auditSessionId,
            String ipAddress,
            String userAgent,
            Instant eventTime) {
        auditEventService.create(buildEventRequest(
                userId,
                auditSessionId,
                AuditEventType.USER_LOGIN,
                AuditAction.LOGIN,
                "Authentication succeeded",
                ipAddress,
                userAgent,
                eventTime));
    }

    public void publishLoginFailure(
            UUID userId,
            UUID auditSessionId,
            AuthenticationFailureReason reason,
            String ipAddress,
            String userAgent,
            Instant eventTime) {
        String description = userId != null
                ? "Authentication failed: " + reason
                : "Authentication failed for unknown user: " + reason;

        auditEventService.create(buildEventRequest(
                userId,
                auditSessionId,
                AuditEventType.USER_LOGIN,
                AuditAction.LOGIN,
                description,
                ipAddress,
                userAgent,
                eventTime));
    }

    public void publishLogout(
            UUID userId,
            UUID auditSessionId,
            String ipAddress,
            String userAgent,
            Instant eventTime) {
        auditEventService.create(buildEventRequest(
                userId,
                auditSessionId,
                AuditEventType.USER_LOGOUT,
                AuditAction.LOGOUT,
                "User logged out",
                ipAddress,
                userAgent,
                eventTime));

        if (auditSessionId != null) {
            auditSessionService.endSession(auditSessionId, eventTime);
        }
    }

    private CreateAuditEventRequest buildEventRequest(
            UUID userId,
            UUID auditSessionId,
            AuditEventType eventType,
            AuditAction action,
            String description,
            String ipAddress,
            String userAgent,
            Instant eventTime) {
        return new CreateAuditEventRequest(
                null,
                "SEC-" + UUID.randomUUID(),
                eventType,
                USER_ENTITY_TYPE,
                userId != null ? userId : UUID.fromString("00000000-0000-0000-0000-000000000000"),
                action,
                description,
                null,
                auditSessionId,
                ipAddress,
                userAgent,
                eventTime,
                AuditEventStatus.RECORDED,
                true);
    }
}
