package com.govos.audit.service;

import com.govos.audit.dto.AuditExportDto;
import com.govos.audit.dto.CreateAuditExportRequest;
import com.govos.audit.dto.UpdateAuditExportRequest;
import com.govos.audit.entity.AuditExport;
import com.govos.audit.entity.AuditExportStatus;
import com.govos.audit.exception.AuditExportException;
import com.govos.audit.exception.AuditExportNotFoundException;
import com.govos.audit.mapper.AuditExportMapper;
import com.govos.audit.repository.AuditExportRepository;
import com.govos.audit.validator.AuditExportValidator;
import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditExportServiceImpl implements AuditExportService {

    private final AuditExportRepository auditExportRepository;
    private final UserRepository userRepository;
    private final AuditExportMapper auditExportMapper;
    private final AuditExportValidator auditExportValidator;

    public AuditExportServiceImpl(
            AuditExportRepository auditExportRepository,
            UserRepository userRepository,
            AuditExportMapper auditExportMapper,
            AuditExportValidator auditExportValidator) {
        this.auditExportRepository = auditExportRepository;
        this.userRepository = userRepository;
        this.auditExportMapper = auditExportMapper;
        this.auditExportValidator = auditExportValidator;
    }

    @Override
    public AuditExportDto getById(UUID id) {
        return auditExportMapper.toDto(findActiveById(id));
    }

    @Override
    public List<AuditExportDto> getByRequestedById(UUID requestedById) {
        return auditExportRepository
                .findByRequestedBy_IdAndDeletedFalseOrderByRequestedTimeDesc(requestedById).stream()
                .map(auditExportMapper::toDto)
                .toList();
    }

    @Override
    public List<AuditExportDto> getByStatus(AuditExportStatus status) {
        return auditExportRepository.findByStatusAndDeletedFalseOrderByRequestedTimeDesc(status).stream()
                .map(auditExportMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AuditExportDto create(CreateAuditExportRequest request) {
        auditExportValidator.validateCreate(request);

        AuditExport entity = new AuditExport();
        entity.setCode(request.code());
        entity.setExportType(request.exportType());
        entity.setRequestedBy(resolveRequestedBy(request.requestedById()));
        entity.setRequestedTime(request.requestedTime());
        entity.setStatus(request.status() != null ? request.status() : AuditExportStatus.PENDING);
        entity.setFileName(request.fileName());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return auditExportMapper.toDto(auditExportRepository.save(entity));
    }

    @Override
    @Transactional
    public AuditExportDto update(UUID id, UpdateAuditExportRequest request) {
        AuditExport entity = findActiveById(id);
        assertVersion(entity, request.version());
        assertNotCompleted(entity, "update");
        auditExportValidator.validateUpdate(id, request);

        if (request.code() != null) {
            entity.setCode(request.code());
        }
        entity.setExportType(request.exportType());
        entity.setRequestedBy(resolveRequestedBy(request.requestedById()));
        entity.setRequestedTime(request.requestedTime());
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        entity.setFileName(request.fileName());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return auditExportMapper.toDto(auditExportRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        AuditExport entity = findActiveById(id);
        assertNotCompleted(entity, "delete");
        entity.setDeleted(true);
        entity.setActive(false);
        auditExportRepository.save(entity);
    }

    private User resolveRequestedBy(UUID requestedById) {
        return userRepository.findByIdAndDeletedFalse(requestedById)
                .orElseThrow(() -> new UserNotFoundException(requestedById));
    }

    private AuditExport findActiveById(UUID id) {
        return auditExportRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuditExportNotFoundException(id));
    }

    private void assertNotCompleted(AuditExport entity, String operation) {
        if (entity.getStatus() == AuditExportStatus.COMPLETED) {
            throw new AuditExportException("Cannot " + operation + " a completed audit export: " + entity.getId());
        }
    }

    private void assertVersion(AuditExport entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "AuditExport version mismatch for id: " + entity.getId());
        }
    }
}
