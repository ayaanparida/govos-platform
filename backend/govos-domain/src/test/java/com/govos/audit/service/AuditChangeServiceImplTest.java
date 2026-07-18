package com.govos.audit.service;

import com.govos.audit.dto.AuditChangeDto;
import com.govos.audit.dto.CreateAuditChangeRequest;
import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditChange;
import com.govos.audit.entity.AuditEvent;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;
import com.govos.audit.exception.AuditChangeNotFoundException;
import com.govos.audit.exception.AuditEventNotFoundException;
import com.govos.audit.mapper.AuditChangeMapper;
import com.govos.audit.repository.AuditChangeRepository;
import com.govos.audit.repository.AuditEventRepository;
import com.govos.audit.validator.AuditChangeValidator;
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
class AuditChangeServiceImplTest {

    @Mock
    private AuditChangeRepository auditChangeRepository;

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private AuditChangeMapper auditChangeMapper;

    @Mock
    private AuditChangeValidator auditChangeValidator;

    @InjectMocks
    private AuditChangeServiceImpl auditChangeService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        AuditChange entity = auditChange(id);
        AuditChangeDto dto = dto(entity);

        when(auditChangeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditChangeMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditChangeService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(auditChangeRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditChangeService.getById(id))
                .isInstanceOf(AuditChangeNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByAuditEventId() {
        UUID auditEventId = UUID.randomUUID();
        AuditChange entity = auditChange(UUID.randomUUID());
        AuditChangeDto dto = dto(entity);

        when(auditChangeRepository.findByAuditEvent_IdAndDeletedFalseOrderByFieldNameAsc(auditEventId))
                .thenReturn(List.of(entity));
        when(auditChangeMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditChangeService.getByAuditEventId(auditEventId)).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        UUID auditEventId = UUID.randomUUID();
        AuditEvent auditEvent = auditEvent(auditEventId);
        CreateAuditChangeRequest request = new CreateAuditChangeRequest(
                "CHG-001", auditEventId, "email", "old@example.com", "new@example.com", true);
        AuditChange saved = auditChange(UUID.randomUUID());
        saved.setAuditEvent(auditEvent);
        AuditChangeDto expected = dto(saved);

        when(auditEventRepository.findByIdAndDeletedFalse(auditEventId)).thenReturn(Optional.of(auditEvent));
        when(auditChangeRepository.save(any(AuditChange.class))).thenReturn(saved);
        when(auditChangeMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditChangeService.create(request)).isEqualTo(expected);
        verify(auditChangeValidator).validateCreate(request);

        ArgumentCaptor<AuditChange> captor = ArgumentCaptor.forClass(AuditChange.class);
        verify(auditChangeRepository).save(captor.capture());
        AuditChange captured = captor.getValue();
        assertThat(captured.getAuditEvent()).isEqualTo(auditEvent);
        assertThat(captured.getFieldName()).isEqualTo("email");
        assertThat(captured.getOldValue()).isEqualTo("old@example.com");
        assertThat(captured.getNewValue()).isEqualTo("new@example.com");
        assertThat(captured.getActive()).isTrue();
        assertThat(captured.getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenCreateAuditEventNotFound() {
        UUID auditEventId = UUID.randomUUID();
        CreateAuditChangeRequest request = new CreateAuditChangeRequest(
                "CHG-001", auditEventId, "email", "old@example.com", "new@example.com", true);

        when(auditEventRepository.findByIdAndDeletedFalse(auditEventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditChangeService.create(request))
                .isInstanceOf(AuditEventNotFoundException.class);
        verify(auditChangeValidator, never()).validateCreate(any());
        verify(auditChangeRepository, never()).save(any());
    }

    private AuditEvent auditEvent(UUID id) {
        AuditEvent entity = new AuditEvent();
        entity.setId(id);
        entity.setCode("AUD-001");
        entity.setEventCode("EVT-001");
        entity.setEventType(AuditEventType.ENTITY_UPDATED);
        entity.setEntityType("User");
        entity.setEntityId(UUID.randomUUID());
        entity.setAction(AuditAction.UPDATE);
        entity.setEventTimestamp(Instant.now());
        entity.setStatus(AuditEventStatus.RECORDED);
        entity.setActive(true);
        entity.setDeleted(false);
        return entity;
    }

    private AuditChange auditChange(UUID id) {
        AuditChange entity = new AuditChange();
        entity.setId(id);
        entity.setCode("CHG-001");
        entity.setAuditEvent(auditEvent(UUID.randomUUID()));
        entity.setFieldName("email");
        entity.setOldValue("old@example.com");
        entity.setNewValue("new@example.com");
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private AuditChangeDto dto(AuditChange entity) {
        UUID auditEventId = entity.getAuditEvent() != null ? entity.getAuditEvent().getId() : null;
        return new AuditChangeDto(
                entity.getId(),
                entity.getCode(),
                auditEventId,
                entity.getFieldName(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
