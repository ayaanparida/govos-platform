package com.govos.audit.service;

import com.govos.audit.dto.AuditActorDto;
import com.govos.audit.dto.CreateAuditActorRequest;
import com.govos.audit.dto.UpdateAuditActorRequest;

import java.util.List;
import java.util.UUID;

public interface AuditActorService {

    AuditActorDto getById(UUID id);

    AuditActorDto getByUserId(UUID userId);

    List<AuditActorDto> getAll();

    AuditActorDto create(CreateAuditActorRequest request);

    AuditActorDto update(UUID id, UpdateAuditActorRequest request);

    void softDelete(UUID id);
}
