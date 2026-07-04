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
class UserRoleServiceImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleServiceImpl userRoleService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        UserRole entity = userRole(id);
        UserRoleDto dto = dto(entity);

        when(userRoleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(userRoleMapper.toDto(entity)).thenReturn(dto);

        assertThat(userRoleService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(userRoleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.getById(id))
                .isInstanceOf(AssignmentNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByUserId() {
        UUID userId = UUID.randomUUID();
        UserRole entity = userRole(UUID.randomUUID());
        UserRoleDto dto = dto(entity);

        when(userRoleRepository.findByUser_IdAndDeletedFalse(userId)).thenReturn(List.of(entity));
        when(userRoleMapper.toDto(entity)).thenReturn(dto);

        assertThat(userRoleService.getByUserId(userId)).containsExactly(dto);
    }

    @Test
    void shouldReturnListWhenGetByRoleId() {
        UUID roleId = UUID.randomUUID();
        UserRole entity = userRole(UUID.randomUUID());
        UserRoleDto dto = dto(entity);

        when(userRoleRepository.findByRole_IdAndDeletedFalse(roleId)).thenReturn(List.of(entity));
        when(userRoleMapper.toDto(entity)).thenReturn(dto);

        assertThat(userRoleService.getByRoleId(roleId)).containsExactly(dto);
    }

    @Test
    void shouldAssignAndReturnDto() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Instant assignedDate = Instant.parse("2026-01-01T00:00:00Z");
        AssignUserRoleRequest request = new AssignUserRoleRequest(
                userId, roleId, assignedDate, null, true);
        User user = user(userId);
        Role role = role(roleId);
        UserRole saved = userRole(UUID.randomUUID());
        UserRoleDto expected = dto(saved);

        when(userRoleRepository.existsByUser_IdAndRole_IdAndDeletedFalse(userId, roleId))
                .thenReturn(false);
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(saved);
        when(userRoleMapper.toDto(saved)).thenReturn(expected);

        assertThat(userRoleService.assign(request)).isEqualTo(expected);

        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getRole()).isEqualTo(role);
        assertThat(captor.getValue().getAssignedDate()).isEqualTo(assignedDate);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenAssignDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AssignUserRoleRequest request = new AssignUserRoleRequest(
                userId, roleId, Instant.now(), null, true);

        when(userRoleRepository.existsByUser_IdAndRole_IdAndDeletedFalse(userId, roleId))
                .thenReturn(true);

        assertThatThrownBy(() -> userRoleService.assign(request))
                .isInstanceOf(DuplicateAssignmentException.class);
        verify(userRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void shouldThrowWhenAssignUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AssignUserRoleRequest request = new AssignUserRoleRequest(
                userId, roleId, Instant.now(), null, true);

        when(userRoleRepository.existsByUser_IdAndRole_IdAndDeletedFalse(userId, roleId))
                .thenReturn(false);
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.assign(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(roleRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void shouldThrowWhenAssignRoleNotFound() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        AssignUserRoleRequest request = new AssignUserRoleRequest(
                userId, roleId, Instant.now(), null, true);

        when(userRoleRepository.existsByUser_IdAndRole_IdAndDeletedFalse(userId, roleId))
                .thenReturn(false);
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user(userId)));
        when(roleRepository.findByIdAndDeletedFalse(roleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRoleService.assign(request))
                .isInstanceOf(RoleNotFoundException.class);
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void shouldRevoke() {
        UUID id = UUID.randomUUID();
        UserRole entity = userRole(id);

        when(userRoleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userRoleService.revoke(id);

        ArgumentCaptor<UserRole> captor = ArgumentCaptor.forClass(UserRole.class);
        verify(userRoleRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    private User user(UUID id) {
        User entity = new User();
        entity.setId(id);
        entity.setUsername("jdoe");
        entity.setEmail("john@example.com");
        entity.setActive(true);
        entity.setDeleted(false);
        return entity;
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

    private UserRole userRole(UUID id) {
        UserRole entity = new UserRole();
        entity.setId(id);
        entity.setCode("UR-001");
        entity.setUser(user(UUID.randomUUID()));
        entity.setRole(role(UUID.randomUUID()));
        entity.setAssignedDate(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private UserRoleDto dto(UserRole entity) {
        return new UserRoleDto(
                entity.getId(),
                entity.getCode(),
                entity.getUser().getId(),
                entity.getRole().getId(),
                entity.getAssignedDate(),
                entity.getExpiryDate(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
