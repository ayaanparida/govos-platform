package com.govos.audit.service;

import com.govos.audit.dto.AuditEventDto;
import com.govos.audit.dto.CreateAuditEventRequest;
import com.govos.audit.entity.AuditAction;
import com.govos.audit.entity.AuditActor;
import com.govos.audit.entity.AuditEvent;
import com.govos.audit.entity.AuditEventStatus;
import com.govos.audit.entity.AuditEventType;
import com.govos.audit.entity.AuditSession;
import com.govos.audit.exception.AuditActorNotFoundException;
import com.govos.audit.exception.AuditEventNotFoundException;
import com.govos.audit.exception.AuditSessionNotFoundException;
import com.govos.audit.mapper.AuditEventMapper;
import com.govos.audit.repository.AuditActorRepository;
import com.govos.audit.repository.AuditEventRepository;
import com.govos.audit.repository.AuditSessionRepository;
import com.govos.audit.validator.AuditEventValidator;
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
class AuditEventServiceImplTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @Mock
    private AuditActorRepository auditActorRepository;

    @Mock
    private AuditSessionRepository auditSessionRepository;

    @Mock
    private AuditEventMapper auditEventMapper;

    @Mock
    private AuditEventValidator auditEventValidator;

    @InjectMocks
    private AuditEventServiceImpl auditEventService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        AuditEvent entity = auditEvent(id, "EVT-001");
        AuditEventDto dto = dto(entity);

        when(auditEventRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditEventMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEventService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(auditEventRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEventService.getById(id))
                .isInstanceOf(AuditEventNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByEventCodeFound() {
        AuditEvent entity = auditEvent(UUID.randomUUID(), "EVT-001");
        AuditEventDto dto = dto(entity);

        when(auditEventRepository.findByEventCodeAndDeletedFalse("EVT-001")).thenReturn(Optional.of(entity));
        when(auditEventMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEventService.getByEventCode("EVT-001")).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByEventCodeNotFound() {
        when(auditEventRepository.findByEventCodeAndDeletedFalse("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEventService.getByEventCode("UNKNOWN"))
                .isInstanceOf(AuditEventNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByEntity() {
        UUID entityId = UUID.randomUUID();
        AuditEvent entity = auditEvent(UUID.randomUUID(), "EVT-001");
        AuditEventDto dto = dto(entity);

        when(auditEventRepository.findByEntityTypeAndEntityIdAndDeletedFalseOrderByEventTimestampDesc(
                "User", entityId)).thenReturn(List.of(entity));
        when(auditEventMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEventService.getByEntity("User", entityId)).containsExactly(dto);
    }

    @Test
    void shouldReturnListWhenGetByActorId() {
        UUID actorId = UUID.randomUUID();
        AuditEvent entity = auditEvent(UUID.randomUUID(), "EVT-001");
        AuditEventDto dto = dto(entity);

        when(auditEventRepository.findByActor_IdAndDeletedFalseOrderByEventTimestampDesc(actorId))
                .thenReturn(List.of(entity));
        when(auditEventMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEventService.getByActorId(actorId)).containsExactly(dto);
    }

    @Test
    void shouldReturnListWhenGetBySessionId() {
        UUID sessionId = UUID.randomUUID();
        AuditEvent entity = auditEvent(UUID.randomUUID(), "EVT-001");
        AuditEventDto dto = dto(entity);

        when(auditEventRepository.findBySession_IdAndDeletedFalseOrderByEventTimestampDesc(sessionId))
                .thenReturn(List.of(entity));
        when(auditEventMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEventService.getBySessionId(sessionId)).containsExactly(dto);
    }

    @Test
    void shouldReturnListWhenGetByStatus() {
        AuditEvent entity = auditEvent(UUID.randomUUID(), "EVT-001");
        AuditEventDto dto = dto(entity);

        when(auditEventRepository.findByStatusAndDeletedFalseOrderByEventTimestampDesc(AuditEventStatus.RECORDED))
                .thenReturn(List.of(entity));
        when(auditEventMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditEventService.getByStatus(AuditEventStatus.RECORDED)).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDtoWithoutActorAndSession() {
        UUID entityId = UUID.randomUUID();
        Instant now = Instant.now();
        CreateAuditEventRequest request = new CreateAuditEventRequest(
                "AUD-001", "EVT-001", AuditEventType.ENTITY_UPDATED, "User", entityId,
                AuditAction.UPDATE, "User updated", null, null,
                "192.168.1.1", "Mozilla/5.0", now, null, null);
        AuditEvent saved = auditEvent(UUID.randomUUID(), "EVT-001");
        AuditEventDto expected = dto(saved);

        when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(saved);
        when(auditEventMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditEventService.create(request)).isEqualTo(expected);
        verify(auditEventValidator).validateCreate(request);
        verify(auditActorRepository, never()).findByIdAndDeletedFalse(any());
        verify(auditSessionRepository, never()).findByIdAndDeletedFalse(any());

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        AuditEvent captured = captor.getValue();
        assertThat(captured.getActor()).isNull();
        assertThat(captured.getSession()).isNull();
        assertThat(captured.getStatus()).isEqualTo(AuditEventStatus.RECORDED);
        assertThat(captured.getActive()).isTrue();
        assertThat(captured.getDeleted()).isFalse();
    }

    @Test
    void shouldCreateAndReturnDtoWithActorAndSession() {
        UUID entityId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();
        AuditActor actor = auditActor(actorId);
        AuditSession session = auditSession(sessionId);
        CreateAuditEventRequest request = new CreateAuditEventRequest(
                "AUD-001", "EVT-002", AuditEventType.USER_LOGIN, "User", entityId,
                AuditAction.LOGIN, "User logged in", actorId, sessionId,
                "192.168.1.1", "Mozilla/5.0", now, AuditEventStatus.RECORDED, true);
        AuditEvent saved = auditEvent(UUID.randomUUID(), "EVT-002");
        saved.setActor(actor);
        saved.setSession(session);
        AuditEventDto expected = dto(saved);

        when(auditActorRepository.findByIdAndDeletedFalse(actorId)).thenReturn(Optional.of(actor));
        when(auditSessionRepository.findByIdAndDeletedFalse(sessionId)).thenReturn(Optional.of(session));
        when(auditEventRepository.save(any(AuditEvent.class))).thenReturn(saved);
        when(auditEventMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditEventService.create(request)).isEqualTo(expected);
        verify(auditEventValidator).validateCreate(request);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditEventRepository).save(captor.capture());
        assertThat(captor.getValue().getActor()).isEqualTo(actor);
        assertThat(captor.getValue().getSession()).isEqualTo(session);
    }

    @Test
    void shouldThrowWhenCreateActorNotFound() {
        UUID actorId = UUID.randomUUID();
        CreateAuditEventRequest request = new CreateAuditEventRequest(
                null, "EVT-003", AuditEventType.ENTITY_CREATED, "User", UUID.randomUUID(),
                AuditAction.CREATE, "Created", actorId, null,
                null, null, Instant.now(), null, null);

        when(auditActorRepository.findByIdAndDeletedFalse(actorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEventService.create(request))
                .isInstanceOf(AuditActorNotFoundException.class);
        verify(auditEventValidator).validateCreate(request);
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCreateSessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        CreateAuditEventRequest request = new CreateAuditEventRequest(
                null, "EVT-004", AuditEventType.USER_LOGIN, "User", UUID.randomUUID(),
                AuditAction.LOGIN, "Login", null, sessionId,
                null, null, Instant.now(), null, null);

        when(auditSessionRepository.findByIdAndDeletedFalse(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEventService.create(request))
                .isInstanceOf(AuditSessionNotFoundException.class);
        verify(auditEventValidator).validateCreate(request);
        verify(auditEventRepository, never()).save(any());
    }

    private AuditEvent auditEvent(UUID id, String eventCode) {
        AuditEvent entity = new AuditEvent();
        entity.setId(id);
        entity.setCode("AUD-001");
        entity.setEventCode(eventCode);
        entity.setEventType(AuditEventType.ENTITY_UPDATED);
        entity.setEntityType("User");
        entity.setEntityId(UUID.randomUUID());
        entity.setAction(AuditAction.UPDATE);
        entity.setDescription("User updated");
        entity.setIpAddress("192.168.1.1");
        entity.setUserAgent("Mozilla/5.0");
        entity.setEventTimestamp(Instant.now());
        entity.setStatus(AuditEventStatus.RECORDED);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private AuditActor auditActor(UUID id) {
        AuditActor actor = new AuditActor();
        actor.setId(id);
        actor.setCode("ACT-001");
        actor.setDisplayName("John Doe");
        actor.setActive(true);
        actor.setDeleted(false);
        return actor;
    }

    private AuditSession auditSession(UUID id) {
        AuditSession session = new AuditSession();
        session.setId(id);
        session.setCode("SES-001");
        session.setSessionId("sess-abc-123");
        session.setLoginTime(Instant.now());
        session.setActive(true);
        session.setDeleted(false);
        return session;
    }

    private AuditEventDto dto(AuditEvent entity) {
        UUID actorId = entity.getActor() != null ? entity.getActor().getId() : null;
        UUID sessionId = entity.getSession() != null ? entity.getSession().getId() : null;
        return new AuditEventDto(
                entity.getId(),
                entity.getCode(),
                entity.getEventCode(),
                entity.getEventType(),
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getAction(),
                entity.getDescription(),
                actorId,
                sessionId,
                entity.getIpAddress(),
                entity.getUserAgent(),
                entity.getEventTimestamp(),
                entity.getStatus(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
