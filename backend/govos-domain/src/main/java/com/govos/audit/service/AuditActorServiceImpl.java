package com.govos.audit.service;

import com.govos.audit.dto.AuditActorDto;
import com.govos.audit.dto.CreateAuditActorRequest;
import com.govos.audit.dto.UpdateAuditActorRequest;
import com.govos.audit.entity.AuditActor;
import com.govos.audit.exception.AuditActorNotFoundException;
import com.govos.audit.mapper.AuditActorMapper;
import com.govos.audit.repository.AuditActorRepository;
import com.govos.audit.validator.AuditActorValidator;
import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuditActorServiceImpl implements AuditActorService {

    private final AuditActorRepository auditActorRepository;
    private final UserRepository userRepository;
    private final AuditActorMapper auditActorMapper;
    private final AuditActorValidator auditActorValidator;

    public AuditActorServiceImpl(
            AuditActorRepository auditActorRepository,
            UserRepository userRepository,
            AuditActorMapper auditActorMapper,
            AuditActorValidator auditActorValidator) {
        this.auditActorRepository = auditActorRepository;
        this.userRepository = userRepository;
        this.auditActorMapper = auditActorMapper;
        this.auditActorValidator = auditActorValidator;
    }

    @Override
    public AuditActorDto getById(UUID id) {
        return auditActorMapper.toDto(findActiveById(id));
    }

    @Override
    public AuditActorDto getByUserId(UUID userId) {
        return auditActorMapper.toDto(findActiveByUserId(userId));
    }

    @Override
    public List<AuditActorDto> getAll() {
        return auditActorRepository.findByDeletedFalseOrderByDisplayNameAsc().stream()
                .map(auditActorMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AuditActorDto create(CreateAuditActorRequest request) {
        auditActorValidator.validateCreate(request);

        AuditActor entity = new AuditActor();
        entity.setCode(request.code());
        entity.setUser(resolveUser(request.userId()));
        entity.setDisplayName(request.displayName());
        entity.setOrganization(request.organization());
        entity.setDepartment(request.department());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return auditActorMapper.toDto(auditActorRepository.save(entity));
    }

    @Override
    @Transactional
    public AuditActorDto update(UUID id, UpdateAuditActorRequest request) {
        AuditActor entity = findActiveById(id);
        assertVersion(entity, request.version());
        auditActorValidator.validateUpdate(id, request);

        if (request.code() != null) {
            entity.setCode(request.code());
        }
        entity.setUser(resolveUser(request.userId()));
        entity.setDisplayName(request.displayName());
        entity.setOrganization(request.organization());
        entity.setDepartment(request.department());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return auditActorMapper.toDto(auditActorRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        AuditActor entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        auditActorRepository.save(entity);
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private AuditActor findActiveById(UUID id) {
        return auditActorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AuditActorNotFoundException(id));
    }

    private AuditActor findActiveByUserId(UUID userId) {
        return auditActorRepository.findByUser_IdAndDeletedFalse(userId)
                .orElseThrow(() -> new AuditActorNotFoundException(userId));
    }

    private void assertVersion(AuditActor entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "AuditActor version mismatch for id: " + entity.getId());
        }
    }
}
