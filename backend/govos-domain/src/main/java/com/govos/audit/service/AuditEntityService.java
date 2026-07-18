package com.govos.audit.service;

import com.govos.audit.dto.AuditEntityDto;
import com.govos.audit.dto.CreateAuditEntityRequest;
import com.govos.audit.dto.UpdateAuditEntityRequest;

import java.util.List;
import java.util.UUID;

public interface AuditEntityService {

    AuditEntityDto getById(UUID id);

    AuditEntityDto getByEntityTypeAndId(String entityType, UUID entityId);

    List<AuditEntityDto> getByEntityType(String entityType);

    AuditEntityDto create(CreateAuditEntityRequest request);

    AuditEntityDto update(UUID id, UpdateAuditEntityRequest request);

    void softDelete(UUID id);
}
