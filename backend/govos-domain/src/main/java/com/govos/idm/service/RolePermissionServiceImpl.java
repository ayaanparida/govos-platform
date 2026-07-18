package com.govos.idm.service;

import com.govos.idm.dto.AssignRolePermissionRequest;
import com.govos.idm.dto.RolePermissionDto;
import com.govos.idm.entity.Permission;
import com.govos.idm.entity.Role;
import com.govos.idm.entity.RolePermission;
import com.govos.idm.exception.AssignmentNotFoundException;
import com.govos.idm.exception.DuplicateAssignmentException;
import com.govos.idm.exception.PermissionNotFoundException;
import com.govos.idm.exception.RoleNotFoundException;
import com.govos.idm.mapper.RolePermissionMapper;
import com.govos.idm.repository.PermissionRepository;
import com.govos.idm.repository.RolePermissionRepository;
import com.govos.idm.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionMapper rolePermissionMapper;

    public RolePermissionServiceImpl(
            RolePermissionRepository rolePermissionRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionMapper rolePermissionMapper) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Override
    public RolePermissionDto getById(UUID id) {
        return rolePermissionMapper.toDto(findActiveById(id));
    }

    @Override
    public List<RolePermissionDto> getByRoleId(UUID roleId) {
        return rolePermissionRepository.findByRole_IdAndDeletedFalse(roleId).stream()
                .map(rolePermissionMapper::toDto)
                .toList();
    }

    @Override
    public List<RolePermissionDto> getByPermissionId(UUID permissionId) {
        return rolePermissionRepository.findByPermission_IdAndDeletedFalse(permissionId).stream()
                .map(rolePermissionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RolePermissionDto assign(AssignRolePermissionRequest request) {
        if (rolePermissionRepository.existsByRole_IdAndPermission_IdAndDeletedFalse(
                request.roleId(), request.permissionId())) {
            throw new DuplicateAssignmentException(
                    "Role already has permission assignment for roleId=" + request.roleId()
                            + ", permissionId=" + request.permissionId());
        }

        Role role = roleRepository.findByIdAndDeletedFalse(request.roleId())
                .orElseThrow(() -> new RoleNotFoundException(request.roleId()));
        Permission permission = permissionRepository.findByIdAndDeletedFalse(request.permissionId())
                .orElseThrow(() -> new PermissionNotFoundException(request.permissionId()));

        RolePermission entity = new RolePermission();
        entity.setRole(role);
        entity.setPermission(permission);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return rolePermissionMapper.toDto(rolePermissionRepository.save(entity));
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        RolePermission entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        rolePermissionRepository.save(entity);
    }

    private RolePermission findActiveById(UUID id) {
        return rolePermissionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AssignmentNotFoundException("RolePermission", id));
    }
}
