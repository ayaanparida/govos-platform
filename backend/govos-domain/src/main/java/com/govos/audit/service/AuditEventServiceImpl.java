package com.govos.audit.service;

import com.govos.audit.dto.AuditEventDto;
import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.entity.AuditActor;
import com.govos.audit.entity.AuditEvent;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditSession;
import com.govos.audit.exception.AuditActorNotFoundException;
import com.govos.audit.exception.AuditEventNotFoundException;
import com.govos.audit.exception.AuditSessionNotFoundException;
import com.govos.audit.mapper.AuditEventMapper;
import com.govos.audit.repository.AuditActorRepository;
import com.govos.audit.repository.AuditEventRepository;
import com.govos.audit.repository.AuditSessionRepository;
import com.govos.audit.validator.AuditEventValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditEventServiceImpl implements AuditEventService {

    private final AuditEventRepository auditEventRepository;
    private final AuditActorRepository auditActorRepository;
    private final AuditSessionRepository auditSessionRepository;
    private final AuditEventMapper auditEventMapper;
    private final AuditEventValidator auditEventValidator;

    public AuditEventServiceImpl(
            AuditEventRepository auditEventRepository,
            AuditActorRepository auditActorRepository,
            AuditSessionRepository auditSessionRepository,
            AuditEventMapper auditEventMapper,
            AuditEventValidator auditEventValidator) {
        this.auditEventRepository = auditEventRepository;
        this.auditActorRepository = auditActorRepository;
        this.auditSessionRepository = auditSessionRepository;
        this.auditEventMapper = auditEventMapper;
        this.auditEventValidator = auditEventValidator;
    }

    @Override
    public AuditEventDto getById(UUID id) {
        return auditEventMapper.toDto(findActiveById(id));
    }

    @Override
    public AuditEventDto getByEventCode(String eventCode) {
        return auditEventMapper.toDto(findActiveByEventCode(eventCode));
    }

    @Override
    public List<AuditEventDto> getByEntity(String entityType, UUID entityId) {
        return auditEventRepository
                .findByEntityTypeAndEntityIdAndDeletedFalseOrderByEventTimestampDesc(entityType, entityId).stream()
                .map(auditEventMapper::toDto)
                .toList();
    }

    @Override
    public List<AuditEventDto> getByActorId(UUID actorId) {
        return auditEventRepository.findByActor_IdAndDeletedFalseOrderByEventTimestampDesc(actorId).stream()
                .map(auditEventMapper::toDto)
                .toList();
    }

    @Override
    public List<AuditEventDto> getBySessionId(UUID sessionId) {
        return auditEventRepository.findBySession_IdAndDeletedFalseOrderByEventTimestampDesc(sessionId).stream()
                .map(auditEventMapper::toDto)
                .toList();
    }

    @Override
    public List<AuditEventDto> getByStatus(AuditEventStatus status) {
        return auditEventRepository.findByStatusAndDeletedFalseOrderByEventTimestampDesc(status).stream()
                .map(auditEventMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AuditEventDto create(CreateAuditEventRequest request) {
        auditEventValidator.validateCreate(request);

        AuditEvent entity = new AuditEvent();
        entity.setCode(request.code());
        entity.setEventCode(request.eventCode());
        entity.setEventType(request.eventType());
        entity.setEntityType(request.entityType());
        entity.setEntityId(request.entityId());
        entity.setAction(request.action());
        entity.setDescription(request.description());
        entity.setActor(resolveActor(request.actorId()));
        entity.setSession(resolveSession(request.sessionId()));
        entity.setIpAddress(request.ipAddress());
        entity.setUserAgent(request.userAgent());
        entity.setEventTimestamp(request.eventTimestamp());
        entity.setStatus(request.status() != null ? request.status() : AuditEventStatus.RECORDED);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return auditEventMapper.toDto(auditEventRepository.save(entity));
    }

    private AuditActor resolveActor(UUID actorId) {
        if (actorId == null) {
            return null;
        }
        return auditActorRepository.findByIdAndDeletedFalse(actorId)
                .orElseThrow(() -> new AuditActorNotFoundException(actorId));
    }

    private AuditSession resolveSession(UUID sessionId) {
        if (sessionId == null) {
            return null;
        }
        return auditSessionRepository.findByIdAndDeletedFalse(sessionId)
                .orElseThrow(() -> new AuditSessionNotFoundException(sessionId));
    }

    private AuditEvent findActiveById(UUID id) {
        return auditEventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuditEventNotFoundException(id));
    }

    private AuditEvent findActiveByEventCode(String eventCode) {
        return auditEventRepository.findByEventCodeAndDeletedFalse(eventCode)
                .orElseThrow(() -> new AuditEventNotFoundException(eventCode));
    }
}
