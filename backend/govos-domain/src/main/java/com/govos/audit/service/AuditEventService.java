package com.govos.audit.service;

import com.govos.audit.dto.AuditEventDto;
import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.entity.AuditEventStatus;

import java.util.List;
import java.util.UUID;

public interface AuditEventService {

    AuditEventDto getById(UUID id);

    AuditEventDto getByEventCode(String eventCode);

    List<AuditEventDto> getByEntity(String entityType, UUID entityId);

    List<AuditEventDto> getByActorId(UUID actorId);

    List<AuditEventDto> getBySessionId(UUID sessionId);

    List<AuditEventDto> getByStatus(AuditEventStatus status);

    AuditEventDto create(CreateAuditEventRequest request);
}
