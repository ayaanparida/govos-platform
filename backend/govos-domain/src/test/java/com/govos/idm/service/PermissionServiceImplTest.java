package com.govos.idm.service;

import com.govos.idm.dto.CreatePermissionRequest;
import com.govos.idm.dto.PermissionDto;
import com.govos.idm.dto.UpdatePermissionRequest;
import com.govos.idm.entity.Permission;
import com.govos.idm.exception.PermissionNotFoundException;
import com.govos.idm.mapper.PermissionMapper;
import com.govos.idm.repository.PermissionRepository;
import com.govos.idm.validator.PermissionValidator;
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
class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private PermissionValidator permissionValidator;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        Permission entity = permission(id, "IDM_USER_READ");
        PermissionDto dto = dto(entity);

        when(permissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(permissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(permissionService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(permissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getById(id))
                .isInstanceOf(PermissionNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByCodeFound() {
        Permission entity = permission(UUID.randomUUID(), "IDM_USER_READ");
        PermissionDto dto = dto(entity);

        when(permissionRepository.findByCodeAndDeletedFalse("IDM_USER_READ"))
                .thenReturn(Optional.of(entity));
        when(permissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(permissionService.getByCode("IDM_USER_READ")).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByCodeNotFound() {
        when(permissionRepository.findByCodeAndDeletedFalse("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> permissionService.getByCode("UNKNOWN"))
                .isInstanceOf(PermissionNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByModule() {
        Permission entity = permission(UUID.randomUUID(), "IDM_USER_READ");
        PermissionDto dto = dto(entity);

        when(permissionRepository.findByModuleAndDeletedFalseOrderByResourceAsc("IDM"))
                .thenReturn(List.of(entity));
        when(permissionMapper.toDto(entity)).thenReturn(dto);

        assertThat(permissionService.getByModule("IDM")).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        CreatePermissionRequest request = new CreatePermissionRequest(
                "IDM_USER_READ", "IDM", "USER", "READ", "Read user records", true);
        Permission entity = permission(UUID.randomUUID(), "IDM_USER_READ");
        PermissionDto expected = dto(entity);

        when(permissionMapper.toEntity(request)).thenReturn(entity);
        when(permissionRepository.save(entity)).thenReturn(entity);
        when(permissionMapper.toDto(entity)).thenReturn(expected);

        assertThat(permissionService.create(request)).isEqualTo(expected);
        verify(permissionValidator).validateCreate(request);
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        Permission entity = permission(id, "IDM_USER_READ");
        entity.setVersion(0L);
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                "IDM_USER_READ", "IDM", "USER", "READ", "Updated description", true, 0L);
        PermissionDto expected = dto(entity);

        when(permissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(permissionRepository.save(entity)).thenReturn(entity);
        when(permissionMapper.toDto(entity)).thenReturn(expected);

        assertThat(permissionService.update(id, request)).isEqualTo(expected);
        verify(permissionValidator).validateUpdate(id, request);
        verify(permissionMapper).updateEntity(request, entity);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        Permission entity = permission(id, "IDM_USER_READ");
        entity.setVersion(1L);
        UpdatePermissionRequest request = new UpdatePermissionRequest(
                "IDM_USER_READ", "IDM", "USER", "READ", "desc", true, 0L);

        when(permissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> permissionService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
        verify(permissionValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        Permission entity = permission(id, "IDM_USER_READ");

        when(permissionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        permissionService.softDelete(id);

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    private Permission permission(UUID id, String code) {
        Permission entity = new Permission();
        entity.setId(id);
        entity.setCode(code);
        entity.setModule("IDM");
        entity.setResource("USER");
        entity.setAction("READ");
        entity.setDescription("Read user records");
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private PermissionDto dto(Permission entity) {
        return new PermissionDto(
                entity.getId(),
                entity.getCode(),
                entity.getModule(),
                entity.getResource(),
                entity.getAction(),
                entity.getDescription(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
