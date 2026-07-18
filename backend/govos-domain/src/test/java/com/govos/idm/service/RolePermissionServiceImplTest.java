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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolePermissionServiceImplTest {

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @InjectMocks
    private RolePermissionServiceImpl rolePermissionService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        RolePermission entity = rolePermission(id);
        RolePermissionDto dto = dto(entity);

        when(rolePermissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(rolePermissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(rolePermissionService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(rolePermissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolePermissionService.getById(id))
                .isInstanceOf(AssignmentNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByRoleId() {
        UUID roleId = UUID.randomUUID();
        RolePermission entity = rolePermission(UUID.randomUUID());
        RolePermissionDto dto = dto(entity);

        when(rolePermissionRepository.findByRole_IdAndDeletedFalse(roleId)).thenReturn(List.of(entity));
        when(rolePermissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(rolePermissionService.getByRoleId(roleId)).containsExactly(dto);
    }

    @Test
    void shouldReturnListWhenGetByPermissionId() {
        UUID permissionId = UUID.randomUUID();
        RolePermission entity = rolePermission(UUID.randomUUID());
        RolePermissionDto dto = dto(entity);

        when(rolePermissionRepository.findByPermission_IdAndDeletedFalse(permissionId))
                .thenReturn(List.of(entity));
        when(rolePermissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(rolePermissionService.getByPermissionId(permissionId)).containsExactly(dto);
    }

    @Test
    void shouldAssignAndReturnDto() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AssignRolePermissionRequest request = new AssignRolePermissionRequest(roleId, permissionId, true);
        Role role = role(roleId);
        Permission permission = permission(permissionId);
        RolePermission saved = rolePermission(UUID.randomUUID());
        RolePermissionDto expected = dto(saved);

        when(rolePermissionRepository.existsByRole_IdAndPermission_IdAndDeletedFalse(roleId, permissionId))
                .thenReturn(false);
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(role));
        when(permissionRepository.findByIdAndDeletedFalse(permissionId)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.save(any(RolePermission.class))).thenReturn(saved);
        when(rolePermissionMapper.toDto(saved)).thenReturn(expected);

        assertThat(rolePermissionService.assign(request)).isEqualTo(expected);

        ArgumentCaptor<RolePermission> captor = ArgumentCaptor.forClass(RolePermission.class);
        verify(rolePermissionRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(role);
        assertThat(captor.getValue().getPermission()).isEqualTo(permission);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenAssignDuplicate() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AssignRolePermissionRequest request = new AssignRolePermissionRequest(roleId, permissionId, true);

        when(rolePermissionRepository.existsByRole_IdAndPermission_IdAndDeletedFalse(roleId, permissionId))
                .thenReturn(true);

        assertThatThrownBy(() -> rolePermissionService.assign(request))
                .isInstanceOf(DuplicateAssignmentException.class);
        verify(roleRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void shouldThrowWhenAssignRoleNotFound() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AssignRolePermissionRequest request = new AssignRolePermissionRequest(roleId, permissionId, true);

        when(rolePermissionRepository.existsByRole_IdAndPermission_IdAndDeletedFalse(roleId, permissionId))
                .thenReturn(false);
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolePermissionService.assign(request))
                .isInstanceOf(RoleNotFoundException.class);
        verify(permissionRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void shouldThrowWhenAssignPermissionNotFound() {
        UUID roleId = UUID.randomUUID();
        UUID permissionId = UUID.randomUUID();
        AssignRolePermissionRequest request = new AssignRolePermissionRequest(roleId, permissionId, true);

        when(rolePermissionRepository.existsByRole_IdAndPermission_IdAndDeletedFalse(roleId, permissionId))
                .thenReturn(false);
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(role(roleId)));
        when(permissionRepository.findByIdAndDeletedFalse(permissionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolePermissionService.assign(request))
                .isInstanceOf(PermissionNotFoundException.class);
        verify(rolePermissionRepository, never()).save(any());
    }

    @Test
    void shouldRevoke() {
        UUID id = UUID.randomUUID();
        RolePermission entity = rolePermission(id);

        when(rolePermissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        rolePermissionService.revoke(id);

        ArgumentCaptor<RolePermission> captor = ArgumentCaptor.forClass(RolePermission.class);
        verify(rolePermissionRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    private Role role(UUID id) {
        Role entity = new Role();
        entity.setId(id);
        entity.setCode("ADMIN");
        entity.setName("Administrator");
        entity.setActive(true);
        entity.setDeleted(false);
        return entity;
    }

    private Permission permission(UUID id) {
        Permission entity = new Permission();
        entity.setId(id);
        entity.setCode("IDM_USER_READ");
        entity.setModule("IDM");
        entity.setResource("USER");
        entity.setAction("READ");
        entity.setActive(true);
        entity.setDeleted(false);
        return entity;
    }

    private RolePermission rolePermission(UUID id) {
        RolePermission entity = new RolePermission();
        entity.setId(id);
        entity.setCode("RP-001");
        entity.setRole(role(UUID.randomUUID()));
        entity.setPermission(permission(UUID.randomUUID()));
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private RolePermissionDto dto(RolePermission entity) {
        return new RolePermissionDto(
                entity.getId(),
                entity.getCode(),
                entity.getRole().getId(),
                entity.getPermission().getId(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
