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
import jakarta.persistence.OptimisticLockException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RoleValidator roleValidator;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        Role entity = role(id, "ADMIN", false);
        RoleDto dto = dto(entity);

        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(roleMapper.toDto(entity)).thenReturn(dto);

        assertThat(roleService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getById(id))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByCodeFound() {
        Role entity = role(UUID.randomUUID(), "ADMIN", false);
        RoleDto dto = dto(entity);

        when(roleRepository.findByCodeAndDeletedFalse("ADMIN")).thenReturn(Optional.of(entity));
        when(roleMapper.toDto(entity)).thenReturn(dto);

        assertThat(roleService.getByCode("ADMIN")).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByCodeNotFound() {
        when(roleRepository.findByCodeAndDeletedFalse("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.getByCode("UNKNOWN"))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetAll() {
        Role entity = role(UUID.randomUUID(), "ADMIN", false);
        RoleDto dto = dto(entity);

        when(roleRepository.findByDeletedFalseOrderByNameAsc()).thenReturn(List.of(entity));
        when(roleMapper.toDto(entity)).thenReturn(dto);

        assertThat(roleService.getAll()).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        CreateRoleRequest request = new CreateRoleRequest(
                "ADMIN", "Administrator", "System administrator role", false, true);
        Role entity = role(UUID.randomUUID(), "ADMIN", false);
        RoleDto expected = dto(entity);

        when(roleMapper.toEntity(request)).thenReturn(entity);
        when(roleRepository.save(entity)).thenReturn(entity);
        when(roleMapper.toDto(entity)).thenReturn(expected);

        assertThat(roleService.create(request)).isEqualTo(expected);
        verify(roleValidator).validateCreate(request);
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getSystemRole()).isFalse();
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        Role entity = role(id, "ADMIN", false);
        entity.setVersion(0L);
        UpdateRoleRequest request = new UpdateRoleRequest(
                "ADMIN", "Administrator Updated", "Updated description", false, true, 0L);
        RoleDto expected = dto(entity);

        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(roleRepository.save(entity)).thenReturn(entity);
        when(roleMapper.toDto(entity)).thenReturn(expected);

        assertThat(roleService.update(id, request)).isEqualTo(expected);
        verify(roleValidator).validateUpdate(id, request);
        verify(roleMapper).updateEntity(request, entity);
    }

    @Test
    void shouldThrowWhenUpdateSystemRole() {
        UUID id = UUID.randomUUID();
        Role entity = role(id, "ADMIN", true);
        UpdateRoleRequest request = new UpdateRoleRequest(
                "ADMIN", "Administrator", "desc", false, true, 0L);

        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> roleService.update(id, request))
                .isInstanceOf(SystemRoleException.class);
        verify(roleValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        Role entity = role(id, "ADMIN", false);
        entity.setVersion(1L);
        UpdateRoleRequest request = new UpdateRoleRequest(
                "ADMIN", "Administrator", "desc", false, true, 0L);

        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> roleService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        Role entity = role(id, "ADMIN", false);

        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        roleService.softDelete(id);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenSoftDeleteSystemRole() {
        UUID id = UUID.randomUUID();
        Role entity = role(id, "ADMIN", true);

        when(roleRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> roleService.softDelete(id))
                .isInstanceOf(SystemRoleException.class);
        verify(roleRepository, never()).save(any());
    }

    private Role role(UUID id, String code, boolean systemRole) {
        Role entity = new Role();
        entity.setId(id);
        entity.setCode(code);
        entity.setName("Administrator");
        entity.setDescription("Admin role");
        entity.setSystemRole(systemRole);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private RoleDto dto(Role entity) {
        return new RoleDto(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getSystemRole(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
