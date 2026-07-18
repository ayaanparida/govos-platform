package com.govos.idm.service;

import com.govos.idm.dto.AssignUserRoleRequest;
import com.govos.idm.dto.UserRoleDto;
import com.govos.idm.entity.Role;
import com.govos.idm.entity.User;
import com.govos.idm.entity.UserRole;
import com.govos.idm.exception.AssignmentNotFoundException;
import com.govos.idm.exception.DuplicateAssignmentException;
import com.govos.idm.exception.RoleNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.UserRoleMapper;
import com.govos.idm.repository.RoleRepository;
import com.govos.idm.repository.UserRepository;
import com.govos.idm.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMapper userRoleMapper;

    public UserRoleServiceImpl(
            UserRoleRepository userRoleRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleMapper userRoleMapper) {
        this.userRoleRepository = userRoleRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleMapper = userRoleMapper;
    }

    @Override
    public UserRoleDto getById(UUID id) {
        return userRoleMapper.toDto(findActiveById(id));
    }

    @Override
    public List<UserRoleDto> getByUserId(UUID userId) {
        return userRoleRepository.findByUser_IdAndDeletedFalse(userId).stream()
                .map(userRoleMapper::toDto)
                .toList();
    }

    @Override
    public List<UserRoleDto> getByRoleId(UUID roleId) {
        return userRoleRepository.findByRole_IdAndDeletedFalse(roleId).stream()
                .map(userRoleMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public UserRoleDto assign(AssignUserRoleRequest request) {
        if (userRoleRepository.existsByUser_IdAndRole_IdAndDeletedFalse(
                request.userId(), request.roleId())) {
            throw new DuplicateAssignmentException(
                    "User already has role assignment for userId=" + request.userId()
                            + ", roleId=" + request.roleId());
        }

        User user = userRepository.findByIdAndDeletedFalse(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        Role role = roleRepository.findByIdAndDeletedFalse(request.roleId())
                .orElseThrow(() -> new RoleNotFoundException(request.roleId()));

        UserRole entity = new UserRole();
        entity.setUser(user);
        entity.setRole(role);
        entity.setAssignedDate(request.assignedDate());
        entity.setExpiryDate(request.expiryDate());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return userRoleMapper.toDto(userRoleRepository.save(entity));
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        UserRole entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        userRoleRepository.save(entity);
    }

    private UserRole findActiveById(UUID id) {
        return userRoleRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AssignmentNotFoundException("UserRole", id));
    }
}
