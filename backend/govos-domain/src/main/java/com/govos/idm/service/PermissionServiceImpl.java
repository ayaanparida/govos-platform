package com.govos.idm.service;

import com.govos.idm.dto.CreatePermissionRequest;
import com.govos.idm.dto.PermissionDto;
import com.govos.idm.dto.UpdatePermissionRequest;
import com.govos.idm.entity.Permission;
import com.govos.idm.exception.PermissionNotFoundException;
import com.govos.idm.mapper.PermissionMapper;
import com.govos.idm.repository.PermissionRepository;
import com.govos.idm.validator.PermissionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final PermissionValidator permissionValidator;

    public PermissionServiceImpl(
            PermissionRepository permissionRepository,
            PermissionMapper permissionMapper,
            PermissionValidator permissionValidator) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
        this.permissionValidator = permissionValidator;
    }

    @Override
    public PermissionDto getById(UUID id) {
        return permissionMapper.toDto(findActiveById(id));
    }

    @Override
    public PermissionDto getByCode(String code) {
        return permissionMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<PermissionDto> getByModule(String module) {
        return permissionRepository.findByModuleAndDeletedFalseOrderByResourceAsc(module).stream()
                .map(permissionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PermissionDto create(CreatePermissionRequest request) {
        permissionValidator.validateCreate(request);

        Permission entity = permissionMapper.toEntity(request);
        applyDefaults(entity, request.active());

        return permissionMapper.toDto(permissionRepository.save(entity));
    }

    @Override
    @Transactional
    public PermissionDto update(UUID id, UpdatePermissionRequest request) {
        Permission entity = findActiveById(id);
        assertVersion(entity, request.version());

        permissionValidator.validateUpdate(id, request);
        permissionMapper.updateEntity(request, entity);
        applyDefaults(entity, request.active());

        return permissionMapper.toDto(permissionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Permission entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        permissionRepository.save(entity);
    }

    private Permission findActiveById(UUID id) {
        return permissionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PermissionNotFoundException(id));
    }

    private Permission findActiveByCode(String code) {
        return permissionRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new PermissionNotFoundException(code));
    }

    private void applyDefaults(Permission entity, Boolean active) {
        if (active != null) {
            entity.setActive(active);
        } else if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
    }

    private void assertVersion(Permission entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Permission version mismatch for id: " + entity.getId());
        }
    }
}
