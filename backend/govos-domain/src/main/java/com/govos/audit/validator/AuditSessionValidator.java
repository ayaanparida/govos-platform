package com.govos.audit.validator;

import com.govos.audit.dto.CreateAuditSessionRequest;
import com.govos.audit.dto.UpdateAuditSessionRequest;
import com.govos.audit.exception.AuditValidationException;
import com.govos.audit.repository.AuditSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuditSessionValidator {

    private final AuditSessionRepository auditSessionRepository;

    public AuditSessionValidator(AuditSessionRepository auditSessionRepository) {
        this.auditSessionRepository = auditSessionRepository;
    }

    public void validateCreate(CreateAuditSessionRequest request) {
        validateLogoutAfterLogin(request.loginTime(), request.logoutTime());
        if (auditSessionRepository.existsBySessionIdAndDeletedFalse(request.sessionId())) {
            throw new AuditValidationException("Audit session already exists with sessionId: " + request.sessionId());
        }
    }

    public void validateUpdate(UUID id, UpdateAuditSessionRequest request) {
        validateLogoutAfterLogin(request.loginTime(), request.logoutTime());
        auditSessionRepository.findBySessionIdAndDeletedFalse(request.sessionId())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new AuditValidationException(
                            "Audit session already exists with sessionId: " + request.sessionId());
                });
    }

    public void validateEndSession(Instant loginTime, Instant logoutTime) {
        validateLogoutAfterLogin(loginTime, logoutTime);
    }

    private void validateLogoutAfterLogin(Instant loginTime, Instant logoutTime) {
        if (loginTime != null && logoutTime != null && !logoutTime.isAfter(loginTime)) {
            throw new AuditValidationException("Logout time must be after login time");
        }
    }
}
