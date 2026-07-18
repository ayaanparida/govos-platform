package com.govos.audit.service;

import com.govos.audit.dto.AuditExportDto;
import com.govos.audit.dto.CreateAuditExportRequest;
import com.govos.audit.dto.UpdateAuditExportRequest;
import com.govos.audit.entity.AuditExportStatus;

import java.util.List;
import java.util.UUID;

public interface AuditExportService {

    AuditExportDto getById(UUID id);

    List<AuditExportDto> getByRequestedById(UUID requestedById);

    List<AuditExportDto> getByStatus(AuditExportStatus status);

    AuditExportDto create(CreateAuditExportRequest request);

    AuditExportDto update(UUID id, UpdateAuditExportRequest request);

    void softDelete(UUID id);
}
