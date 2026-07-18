package com.govos.audit.service;

import com.govos.audit.dto.AuditExportDto;
import com.govos.audit.dto.CreateAuditExportRequest;
import com.govos.audit.dto.UpdateAuditExportRequest;
import com.govos.audit.entity.AuditExport;
import com.govos.audit.entity.AuditExportStatus;
import com.govos.audit.entity.AuditExportType;
import com.govos.audit.exception.AuditExportException;
import com.govos.audit.exception.AuditExportNotFoundException;
import com.govos.audit.mapper.AuditExportMapper;
import com.govos.audit.repository.AuditExportRepository;
import com.govos.audit.validator.AuditExportValidator;
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
class AuditExportServiceImplTest {

    @Mock
    private AuditExportRepository auditExportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditExportMapper auditExportMapper;

    @Mock
    private AuditExportValidator auditExportValidator;

    @InjectMocks
    private AuditExportServiceImpl auditExportService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        AuditExport entity = auditExport(id, AuditExportStatus.PENDING);
        AuditExportDto dto = dto(entity);

        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditExportMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditExportService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditExportService.getById(id))
                .isInstanceOf(AuditExportNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByRequestedById() {
        UUID requestedById = UUID.randomUUID();
        AuditExport entity = auditExport(UUID.randomUUID(), AuditExportStatus.PENDING);
        AuditExportDto dto = dto(entity);

        when(auditExportRepository.findByRequestedBy_IdAndDeletedFalseOrderByRequestedTimeDesc(requestedById))
                .thenReturn(List.of(entity));
        when(auditExportMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditExportService.getByRequestedById(requestedById)).containsExactly(dto);
    }

    @Test
    void shouldReturnListWhenGetByStatus() {
        AuditExport entity = auditExport(UUID.randomUUID(), AuditExportStatus.PENDING);
        AuditExportDto dto = dto(entity);

        when(auditExportRepository.findByStatusAndDeletedFalseOrderByRequestedTimeDesc(AuditExportStatus.PENDING))
                .thenReturn(List.of(entity));
        when(auditExportMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditExportService.getByStatus(AuditExportStatus.PENDING)).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        UUID requestedById = UUID.randomUUID();
        User user = user(requestedById);
        Instant requestedTime = Instant.now();
        CreateAuditExportRequest request = new CreateAuditExportRequest(
                "EXP-001", AuditExportType.CSV, requestedById, requestedTime,
                null, "audit-export.csv", true);
        AuditExport saved = auditExport(UUID.randomUUID(), AuditExportStatus.PENDING);
        saved.setRequestedBy(user);
        AuditExportDto expected = dto(saved);

        when(userRepository.findByIdAndDeletedFalse(requestedById)).thenReturn(Optional.of(user));
        when(auditExportRepository.save(any(AuditExport.class))).thenReturn(saved);
        when(auditExportMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditExportService.create(request)).isEqualTo(expected);
        verify(auditExportValidator).validateCreate(request);

        ArgumentCaptor<AuditExport> captor = ArgumentCaptor.forClass(AuditExport.class);
        verify(auditExportRepository).save(captor.capture());
        assertThat(captor.getValue().getRequestedBy()).isEqualTo(user);
        assertThat(captor.getValue().getStatus()).isEqualTo(AuditExportStatus.PENDING);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenCreateUserNotFound() {
        UUID requestedById = UUID.randomUUID();
        CreateAuditExportRequest request = new CreateAuditExportRequest(
                "EXP-001", AuditExportType.CSV, requestedById, Instant.now(),
                null, "audit-export.csv", true);

        when(userRepository.findByIdAndDeletedFalse(requestedById)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditExportService.create(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(auditExportValidator).validateCreate(request);
        verify(auditExportRepository, never()).save(any());
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        UUID requestedById = UUID.randomUUID();
        User user = user(requestedById);
        Instant requestedTime = Instant.now();
        AuditExport entity = auditExport(id, AuditExportStatus.PENDING);
        entity.setVersion(0L);
        UpdateAuditExportRequest request = new UpdateAuditExportRequest(
                "EXP-001", AuditExportType.JSON, requestedById, requestedTime,
                AuditExportStatus.IN_PROGRESS, "audit-export.json", false, 0L);
        AuditExportDto expected = dto(entity);

        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(userRepository.findByIdAndDeletedFalse(requestedById)).thenReturn(Optional.of(user));
        when(auditExportRepository.save(entity)).thenReturn(entity);
        when(auditExportMapper.toDto(entity)).thenReturn(expected);

        assertThat(auditExportService.update(id, request)).isEqualTo(expected);
        verify(auditExportValidator).validateUpdate(id, request);
        assertThat(entity.getExportType()).isEqualTo(AuditExportType.JSON);
        assertThat(entity.getStatus()).isEqualTo(AuditExportStatus.IN_PROGRESS);
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        AuditExport entity = auditExport(id, AuditExportStatus.PENDING);
        entity.setVersion(1L);
        UpdateAuditExportRequest request = new UpdateAuditExportRequest(
                "EXP-001", AuditExportType.CSV, UUID.randomUUID(), Instant.now(),
                AuditExportStatus.IN_PROGRESS, "audit-export.csv", true, 0L);

        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> auditExportService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
        verify(auditExportValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldThrowWhenUpdateCompletedExport() {
        UUID id = UUID.randomUUID();
        AuditExport entity = auditExport(id, AuditExportStatus.COMPLETED);
        entity.setVersion(0L);
        UpdateAuditExportRequest request = new UpdateAuditExportRequest(
                "EXP-001", AuditExportType.CSV, UUID.randomUUID(), Instant.now(),
                AuditExportStatus.IN_PROGRESS, "audit-export.csv", true, 0L);

        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> auditExportService.update(id, request))
                .isInstanceOf(AuditExportException.class);
        verify(auditExportValidator, never()).validateUpdate(eq(id), any());
        verify(auditExportRepository, never()).save(any());
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        AuditExport entity = auditExport(id, AuditExportStatus.PENDING);

        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        auditExportService.softDelete(id);

        ArgumentCaptor<AuditExport> captor = ArgumentCaptor.forClass(AuditExport.class);
        verify(auditExportRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenSoftDeleteCompletedExport() {
        UUID id = UUID.randomUUID();
        AuditExport entity = auditExport(id, AuditExportStatus.COMPLETED);

        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> auditExportService.softDelete(id))
                .isInstanceOf(AuditExportException.class);
        verify(auditExportRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSoftDeleteNotFound() {
        UUID id = UUID.randomUUID();
        when(auditExportRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditExportService.softDelete(id))
                .isInstanceOf(AuditExportNotFoundException.class);
        verify(auditExportRepository, never()).save(any());
    }

    private AuditExport auditExport(UUID id, AuditExportStatus status) {
        AuditExport entity = new AuditExport();
        entity.setId(id);
        entity.setCode("EXP-001");
        entity.setExportType(AuditExportType.CSV);
        entity.setRequestedBy(user(UUID.randomUUID()));
        entity.setRequestedTime(Instant.now());
        entity.setStatus(status);
        entity.setFileName("audit-export.csv");
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

    private AuditExportDto dto(AuditExport entity) {
        UUID requestedById = entity.getRequestedBy() != null ? entity.getRequestedBy().getId() : null;
        return new AuditExportDto(
                entity.getId(),
                entity.getCode(),
                entity.getExportType(),
                requestedById,
                entity.getRequestedTime(),
                entity.getStatus(),
                entity.getFileName(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
