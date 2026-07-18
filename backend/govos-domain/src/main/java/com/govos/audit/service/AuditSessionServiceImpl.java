package com.govos.audit.service;

import com.govos.audit.dto.AuditSessionDto;
import com.govos.audit.dto.CreateAuditSessionRequest;
import com.govos.audit.dto.UpdateAuditSessionRequest;
import com.govos.audit.entity.AuditSession;
import com.govos.audit.exception.AuditSessionNotFoundException;
import com.govos.audit.mapper.AuditSessionMapper;
import com.govos.audit.repository.AuditSessionRepository;
import com.govos.audit.validator.AuditSessionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditSessionServiceImpl implements AuditSessionService {

    private final AuditSessionRepository auditSessionRepository;
    private final AuditSessionMapper auditSessionMapper;
    private final AuditSessionValidator auditSessionValidator;

    public AuditSessionServiceImpl(
            AuditSessionRepository auditSessionRepository,
            AuditSessionMapper auditSessionMapper,
            AuditSessionValidator auditSessionValidator) {
        this.auditSessionRepository = auditSessionRepository;
        this.auditSessionMapper = auditSessionMapper;
        this.auditSessionValidator = auditSessionValidator;
    }

    @Override
    public AuditSessionDto getById(UUID id) {
        return auditSessionMapper.toDto(findActiveById(id));
    }

    @Override
    public AuditSessionDto getBySessionId(String sessionId) {
        return auditSessionMapper.toDto(findActiveBySessionId(sessionId));
    }

    @Override
    @Transactional
    public AuditSessionDto create(CreateAuditSessionRequest request) {
        auditSessionValidator.validateCreate(request);

        AuditSession entity = new AuditSession();
        entity.setCode(request.code());
        entity.setSessionId(request.sessionId());
        entity.setLoginTime(request.loginTime());
        entity.setLogoutTime(request.logoutTime());
        entity.setIpAddress(request.ipAddress());
        entity.setDevice(request.device());
        entity.setBrowser(request.browser());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return auditSessionMapper.toDto(auditSessionRepository.save(entity));
    }

    @Override
    @Transactional
    public AuditSessionDto update(UUID id, UpdateAuditSessionRequest request) {
        AuditSession entity = findActiveById(id);
        assertVersion(entity, request.version());
        auditSessionValidator.validateUpdate(id, request);

        if (request.code() != null) {
            entity.setCode(request.code());
        }
        entity.setSessionId(request.sessionId());
        entity.setLoginTime(request.loginTime());
        entity.setLogoutTime(request.logoutTime());
        entity.setIpAddress(request.ipAddress());
        entity.setDevice(request.device());
        entity.setBrowser(request.browser());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return auditSessionMapper.toDto(auditSessionRepository.save(entity));
    }

    @Override
    @Transactional
    public AuditSessionDto endSession(UUID id, Instant logoutTime) {
        AuditSession entity = findActiveById(id);
        auditSessionValidator.validateEndSession(entity.getLoginTime(), logoutTime);
        entity.setLogoutTime(logoutTime);
        return auditSessionMapper.toDto(auditSessionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        AuditSession entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        auditSessionRepository.save(entity);
    }

    private AuditSession findActiveById(UUID id) {
        return auditSessionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuditSessionNotFoundException(id));
    }

    private AuditSession findActiveBySessionId(String sessionId) {
        return auditSessionRepository.findBySessionIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new AuditSessionNotFoundException(sessionId));
    }

    private void assertVersion(AuditSession entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "AuditSession version mismatch for id: " + entity.getId());
        }
    }
}
