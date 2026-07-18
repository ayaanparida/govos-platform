package com.govos.idm.service;

import com.govos.idm.dto.CreateRoleRequest;
import com.govos.idm.dto.RoleDto;
import com.govos.idm.dto.UpdateRoleRequest;
import com.govos.idm.entity.Role;
import com.govos.idm.exception.RoleNotFoundException;
import com.govos.idm.exception.SystemRoleException;
import com.govos.idm.mapper.RoleMapper;
import com.govos.idm.repository.RoleRepository;
import com.govos.idm.validator.RoleValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final RoleValidator roleValidator;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper, RoleValidator roleValidator) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.roleValidator = roleValidator;
    }

    @Override
    public RoleDto getById(UUID id) {
        return roleMapper.toDto(findActiveById(id));
    }

    @Override
    public RoleDto getByCode(String code) {
        return roleMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<RoleDto> getAll() {
        return roleRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RoleDto create(CreateRoleRequest request) {
        roleValidator.validateCreate(request);

        Role entity = roleMapper.toEntity(request);
        applyDefaults(entity, request.active(), request.systemRole());

        return roleMapper.toDto(roleRepository.save(entity));
    }

    @Override
    @Transactional
    public RoleDto update(UUID id, UpdateRoleRequest request) {
        Role entity = findActiveById(id);
        assertNotSystemRole(entity, "updated");
        assertVersion(entity, request.version());

        roleValidator.validateUpdate(id, request);
        roleMapper.updateEntity(request, entity);
        applyDefaults(entity, request.active(), request.systemRole());

        return roleMapper.toDto(roleRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Role entity = findActiveById(id);
        assertNotSystemRole(entity, "deleted");

        entity.setDeleted(true);
        entity.setActive(false);
        roleRepository.save(entity);
    }

    private Role findActiveById(UUID id) {
        return roleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RoleNotFoundException(id));
    }

    private Role findActiveByCode(String code) {
        return roleRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new RoleNotFoundException(code));
    }

    private void applyDefaults(Role entity, Boolean active, Boolean systemRole) {
        if (active != null) {
            entity.setActive(active);
        } else if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (systemRole != null) {
            entity.setSystemRole(systemRole);
        } else if (entity.getSystemRole() == null) {
            entity.setSystemRole(false);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
    }

    private void assertNotSystemRole(Role entity, String operation) {
        if (Boolean.TRUE.equals(entity.getSystemRole())) {
            throw new SystemRoleException(operation);
        }
    }

    private void assertVersion(Role entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Role version mismatch for id: " + entity.getId());
        }
    }
}
