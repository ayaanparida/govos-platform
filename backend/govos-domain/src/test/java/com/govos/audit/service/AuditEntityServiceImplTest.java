package com.govos.audit.service;

import com.govos.audit.dto.AuditEntityDto;
import com.govos.audit.dto.CreateAuditEntityRequest;
import com.govos.audit.dto.UpdateAuditEntityRequest;
import com.govos.audit.entity.AuditEntity;
import com.govos.audit.exception.AuditEntityNotFoundException;
import com.govos.audit.mapper.AuditEntityMapper;
import com.govos.audit.repository.AuditEntityRepository;
import com.govos.audit.validator.AuditEntityValidator;
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
class AuditEntityServiceImplTest {

    @Mock
    private AuditEntityRepository auditEntityRepository;

    @Mock
    private AuditEntityMapper auditEntityMapper;

    @Mock
    private AuditEntityValidator auditEntityValidator;

    @InjectMocks
    private AuditEntityServiceImpl auditEntityService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        AuditEntity entity = auditEntity(id);
        AuditEntityDto dto = dto(entity);

        when(auditEntityRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditEntityMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEntityService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(auditEntityRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEntityService.getById(id))
                .isInstanceOf(AuditEntityNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByEntityTypeAndIdFound() {
        UUID entityId = UUID.randomUUID();
        AuditEntity entity = auditEntity(UUID.randomUUID());
        AuditEntityDto dto = dto(entity);

        when(auditEntityRepository.findByEntityTypeAndEntityIdAndDeletedFalse("User", entityId))
                .thenReturn(Optional.of(entity));
        when(auditEntityMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEntityService.getByEntityTypeAndId("User", entityId)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByEntityTypeAndIdNotFound() {
        UUID entityId = UUID.randomUUID();
        when(auditEntityRepository.findByEntityTypeAndEntityIdAndDeletedFalse("User", entityId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEntityService.getByEntityTypeAndId("User", entityId))
                .isInstanceOf(AuditEntityNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByEntityType() {
        AuditEntity entity = auditEntity(UUID.randomUUID());
        AuditEntityDto dto = dto(entity);

        when(auditEntityRepository.findByEntityTypeAndDeletedFalseOrderByEntityNameAsc("User"))
                .thenReturn(List.of(entity));
        when(auditEntityMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEntityService.getByEntityType("User")).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        UUID entityId = UUID.randomUUID();
        CreateAuditEntityRequest request = new CreateAuditEntityRequest(
                "ENT-001", "User", entityId, "John Doe", true);
        AuditEntity saved = auditEntity(UUID.randomUUID());
        AuditEntityDto expected = dto(saved);

        when(auditEntityRepository.save(any(AuditEntity.class))).thenReturn(saved);
        when(auditEntityMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditEntityService.create(request)).isEqualTo(expected);
        verify(auditEntityValidator).validateCreate(request);

        ArgumentCaptor<AuditEntity> captor = ArgumentCaptor.forClass(AuditEntity.class);
        verify(auditEntityRepository).save(captor.capture());
        assertThat(captor.getValue().getEntityType()).isEqualTo("User");
        assertThat(captor.getValue().getEntityId()).isEqualTo(entityId);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        AuditEntity entity = auditEntity(id);
        entity.setVersion(0L);
        UpdateAuditEntityRequest request = new UpdateAuditEntityRequest(
                "ENT-001", "Role", entityId, "Administrator", false, 0L);
        AuditEntityDto expected = dto(entity);

        when(auditEntityRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditEntityRepository.save(entity)).thenReturn(entity);
        when(auditEntityMapper.toDto(entity)).thenReturn(expected);

        assertThat(auditEntityService.update(id, request)).isEqualTo(expected);
        verify(auditEntityValidator).validateUpdate(id, request);
        assertThat(entity.getEntityType()).isEqualTo("Role");
        assertThat(entity.getEntityName()).isEqualTo("Administrator");
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        AuditEntity entity = auditEntity(id);
        entity.setVersion(1L);
        UpdateAuditEntityRequest request = new UpdateAuditEntityRequest(
                "ENT-001", "User", UUID.randomUUID(), "John Doe", true, 0L);

        when(auditEntityRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> auditEntityService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
        verify(auditEntityValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        AuditEntity entity = auditEntity(id);

        when(auditEntityRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        auditEntityService.softDelete(id);

        ArgumentCaptor<AuditEntity> captor = ArgumentCaptor.forClass(AuditEntity.class);
        verify(auditEntityRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenSoftDeleteNotFound() {
        UUID id = UUID.randomUUID();
        when(auditEntityRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEntityService.softDelete(id))
                .isInstanceOf(AuditEntityNotFoundException.class);
        verify(auditEntityRepository, never()).save(any());
    }

    private AuditEntity auditEntity(UUID id) {
        AuditEntity entity = new AuditEntity();
        entity.setId(id);
        entity.setCode("ENT-001");
        entity.setEntityType("User");
        entity.setEntityId(UUID.randomUUID());
        entity.setEntityName("John Doe");
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private AuditEntityDto dto(AuditEntity entity) {
        return new AuditEntityDto(
                entity.getId(),
                entity.getCode(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getEntityName(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
