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
class AuditActorServiceImplTest {

    @Mock
    private AuditActorRepository auditActorRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditActorMapper auditActorMapper;

    @Mock
    private AuditActorValidator auditActorValidator;

    @InjectMocks
    private AuditActorServiceImpl auditActorService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        AuditActor entity = auditActor(id);
        AuditActorDto dto = dto(entity);

        when(auditActorRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditActorMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditActorService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(auditActorRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditActorService.getById(id))
                .isInstanceOf(AuditActorNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByUserIdFound() {
        UUID userId = UUID.randomUUID();
        AuditActor entity = auditActor(UUID.randomUUID());
        entity.setUser(user(userId));
        AuditActorDto dto = dto(entity);

        when(auditActorRepository.findByUser_IdAndDeletedFalse(userId)).thenReturn(Optional.of(entity));
        when(auditActorMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditActorService.getByUserId(userId)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByUserIdNotFound() {
        UUID userId = UUID.randomUUID();
        when(auditActorRepository.findByUser_IdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditActorService.getByUserId(userId))
                .isInstanceOf(AuditActorNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetAll() {
        AuditActor entity = auditActor(UUID.randomUUID());
        AuditActorDto dto = dto(entity);

        when(auditActorRepository.findByDeletedFalseOrderByDisplayNameAsc()).thenReturn(List.of(entity));
        when(auditActorMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditActorService.getAll()).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        CreateAuditActorRequest request = new CreateAuditActorRequest(
                "ACT-001", userId, "John Doe", "GovOS", "IT", true);
        AuditActor saved = auditActor(UUID.randomUUID());
        saved.setUser(user);
        AuditActorDto expected = dto(saved);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(auditActorRepository.save(any(AuditActor.class))).thenReturn(saved);
        when(auditActorMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditActorService.create(request)).isEqualTo(expected);
        verify(auditActorValidator).validateCreate(request);

        ArgumentCaptor<AuditActor> captor = ArgumentCaptor.forClass(AuditActor.class);
        verify(auditActorRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenCreateUserNotFound() {
        UUID userId = UUID.randomUUID();
        CreateAuditActorRequest request = new CreateAuditActorRequest(
                "ACT-001", userId, "John Doe", "GovOS", "IT", true);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditActorService.create(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(auditActorValidator).validateCreate(request);
        verify(auditActorRepository, never()).save(any());
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = user(userId);
        AuditActor entity = auditActor(id);
        entity.setVersion(0L);
        UpdateAuditActorRequest request = new UpdateAuditActorRequest(
                "ACT-001", userId, "Jane Doe", "GovOS", "HR", false, 0L);
        AuditActorDto expected = dto(entity);

        when(auditActorRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(auditActorRepository.save(entity)).thenReturn(entity);
        when(auditActorMapper.toDto(entity)).thenReturn(expected);

        assertThat(auditActorService.update(id, request)).isEqualTo(expected);
        verify(auditActorValidator).validateUpdate(id, request);
        assertThat(entity.getDisplayName()).isEqualTo("Jane Doe");
        assertThat(entity.getUser()).isEqualTo(user);
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        AuditActor entity = auditActor(id);
        entity.setVersion(1L);
        UpdateAuditActorRequest request = new UpdateAuditActorRequest(
                "ACT-001", null, "Jane Doe", "GovOS", "HR", true, 0L);

        when(auditActorRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> auditActorService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
        verify(auditActorValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        AuditActor entity = auditActor(id);

        when(auditActorRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        auditActorService.softDelete(id);

        ArgumentCaptor<AuditActor> captor = ArgumentCaptor.forClass(AuditActor.class);
        verify(auditActorRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenSoftDeleteNotFound() {
        UUID id = UUID.randomUUID();
        when(auditActorRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditActorService.softDelete(id))
                .isInstanceOf(AuditActorNotFoundException.class);
        verify(auditActorRepository, never()).save(any());
    }

    private AuditActor auditActor(UUID id) {
        AuditActor entity = new AuditActor();
        entity.setId(id);
        entity.setCode("ACT-001");
        entity.setDisplayName("John Doe");
        entity.setOrganization("GovOS");
        entity.setDepartment("IT");
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private User user(UUID id) {
        User user = new User();
        user.setId(id);
        user.setUsername("jdoe");
        user.setEmail("john@example.com");
        user.setActive(true);
        user.setDeleted(false);
        return user;
    }

    private AuditActorDto dto(AuditActor entity) {
        UUID userId = entity.getUser() != null ? entity.getUser().getId() : null;
        return new AuditActorDto(
                entity.getId(),
                entity.getCode(),
                userId,
                entity.getDisplayName(),
                entity.getOrganization(),
                entity.getDepartment(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
