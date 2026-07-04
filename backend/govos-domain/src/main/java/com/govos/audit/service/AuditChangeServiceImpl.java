package com.govos.audit.service;

import com.govos.audit.dto.AuditChangeDto;
import com.govos.audit.dto.CreateAuditChangeRequest;
import com.govos.audit.entity.AuditChange;
import com.govos.audit.entity.AuditEvent;
import com.govos.audit.exception.AuditChangeNotFoundException;
import com.govos.audit.exception.AuditEventNotFoundException;
import com.govos.audit.mapper.AuditChangeMapper;
import com.govos.audit.repository.AuditChangeRepository;
import com.govos.audit.repository.AuditEventRepository;
import com.govos.audit.validator.AuditChangeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditChangeServiceImpl implements AuditChangeService {

    private final AuditChangeRepository auditChangeRepository;
    private final AuditEventRepository auditEventRepository;
    private final AuditChangeMapper auditChangeMapper;
    private final AuditChangeValidator auditChangeValidator;

    public AuditChangeServiceImpl(
            AuditChangeRepository auditChangeRepository,
            AuditEventRepository auditEventRepository,
            AuditChangeMapper auditChangeMapper,
            AuditChangeValidator auditChangeValidator) {
        this.auditChangeRepository = auditChangeRepository;
        this.auditEventRepository = auditEventRepository;
        this.auditChangeMapper = auditChangeMapper;
        this.auditChangeValidator = auditChangeValidator;
    }

    @Override
    public AuditChangeDto getById(UUID id) {
        return auditChangeMapper.toDto(findActiveById(id));
    }

    @Override
    public List<AuditChangeDto> getByAuditEventId(UUID auditEventId) {
        return auditChangeRepository.findByAuditEvent_IdAndDeletedFalseOrderByFieldNameAsc(auditEventId).stream()
                .map(auditChangeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AuditChangeDto create(CreateAuditChangeRequest request) {
        AuditEvent auditEvent = resolveAuditEvent(request.auditEventId());
        auditChangeValidator.validateCreate(request);

        AuditChange entity = new AuditChange();
        entity.setCode(request.code());
        entity.setAuditEvent(auditEvent);
        entity.setFieldName(request.fieldName());
        entity.setOldValue(request.oldValue());
        entity.setNewValue(request.newValue());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return auditChangeMapper.toDto(auditChangeRepository.save(entity));
    }

    private AuditEvent resolveAuditEvent(UUID auditEventId) {
        return auditEventRepository.findByIdAndDeletedFalse(auditEventId)
                .orElseThrow(() -> new AuditEventNotFoundException(auditEventId));
    }

    private AuditChange findActiveById(UUID id) {
        return auditChangeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuditChangeNotFoundException(id));
    }
}
