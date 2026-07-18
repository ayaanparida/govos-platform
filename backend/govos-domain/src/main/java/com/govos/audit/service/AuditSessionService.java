package com.govos.audit.service;

import com.govos.audit.dto.AuditSessionDto;
import com.govos.audit.dto.CreateAuditSessionRequest;
import com.govos.audit.dto.UpdateAuditSessionRequest;

import java.time.Instant;
import java.util.UUID;

public interface AuditSessionService {

    AuditSessionDto getById(UUID id);

    AuditSessionDto getBySessionId(String sessionId);

    AuditSessionDto create(CreateAuditSessionRequest request);

    AuditSessionDto update(UUID id, UpdateAuditSessionRequest request);

    AuditSessionDto endSession(UUID id, Instant logoutTime);

    void softDelete(UUID id);
}
