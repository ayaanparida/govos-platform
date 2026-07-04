package com.govos.audit.service;

import com.govos.audit.dto.AuditChangeDto;
import com.govos.audit.dto.CreateAuditChangeRequest;

import java.util.List;
import java.util.UUID;

public interface AuditChangeService {

    AuditChangeDto getById(UUID id);

    List<AuditChangeDto> getByAuditEventId(UUID auditEventId);

    AuditChangeDto create(CreateAuditChangeRequest request);
}
