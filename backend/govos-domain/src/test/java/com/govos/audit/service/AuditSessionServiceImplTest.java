package com.govos.audit.service;

import com.govos.audit.dto.AuditSessionDto;
import com.govos.audit.dto.CreateAuditSessionRequest;
import com.govos.audit.dto.UpdateAuditSessionRequest;
import com.govos.audit.entity.AuditSession;
import com.govos.audit.exception.AuditSessionNotFoundException;
import com.govos.audit.mapper.AuditSessionMapper;
import com.govos.audit.repository.AuditSessionRepository;
import com.govos.audit.validator.AuditSessionValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
class AuditSessionServiceImplTest {

    @Mock
    private AuditSessionRepository auditSessionRepository;

    @Mock
    private AuditSessionMapper auditSessionMapper;

    @Mock
    private AuditSessionValidator auditSessionValidator;

    @InjectMocks
    private AuditSessionServiceImpl auditSessionService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        AuditSession entity = auditSession(id);
        AuditSessionDto dto = dto(entity);

        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditSessionMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditSessionService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditSessionService.getById(id))
                .isInstanceOf(AuditSessionNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetBySessionIdFound() {
        AuditSession entity = auditSession(UUID.randomUUID());
        AuditSessionDto dto = dto(entity);

        when(auditSessionRepository.findBySessionIdAndDeletedFalse("sess-abc-123"))
                .thenReturn(Optional.of(entity));
        when(auditSessionMapper.toDto(entity)).thenReturn(dto);

        assertThat(auditSessionService.getBySessionId("sess-abc-123")).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetBySessionIdNotFound() {
        when(auditSessionRepository.findBySessionIdAndDeletedFalse("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditSessionService.getBySessionId("unknown"))
                .isInstanceOf(AuditSessionNotFoundException.class);
    }

    @Test
    void shouldCreateAndReturnDto() {
        Instant loginTime = Instant.now();
        CreateAuditSessionRequest request = new CreateAuditSessionRequest(
                "SES-001", "sess-abc-123", loginTime, null,
                "192.168.1.1", "Desktop", "Chrome", true);
        AuditSession saved = auditSession(UUID.randomUUID());
        AuditSessionDto expected = dto(saved);

        when(auditSessionRepository.save(any(AuditSession.class))).thenReturn(saved);
        when(auditSessionMapper.toDto(saved)).thenReturn(expected);

        assertThat(auditSessionService.create(request)).isEqualTo(expected);
        verify(auditSessionValidator).validateCreate(request);

        ArgumentCaptor<AuditSession> captor = ArgumentCaptor.forClass(AuditSession.class);
        verify(auditSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getSessionId()).isEqualTo("sess-abc-123");
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        Instant loginTime = Instant.now();
        Instant logoutTime = loginTime.plusSeconds(3600);
        AuditSession entity = auditSession(id);
        entity.setVersion(0L);
        UpdateAuditSessionRequest request = new UpdateAuditSessionRequest(
                "SES-001", "sess-updated", loginTime, logoutTime,
                "10.0.0.1", "Mobile", "Safari", false, 0L);
        AuditSessionDto expected = dto(entity);

        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditSessionRepository.save(entity)).thenReturn(entity);
        when(auditSessionMapper.toDto(entity)).thenReturn(expected);

        assertThat(auditSessionService.update(id, request)).isEqualTo(expected);
        verify(auditSessionValidator).validateUpdate(id, request);
        assertThat(entity.getSessionId()).isEqualTo("sess-updated");
        assertThat(entity.getLogoutTime()).isEqualTo(logoutTime);
        assertThat(entity.getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        AuditSession entity = auditSession(id);
        entity.setVersion(1L);
        UpdateAuditSessionRequest request = new UpdateAuditSessionRequest(
                "SES-001", "sess-abc-123", Instant.now(), null,
                "192.168.1.1", "Desktop", "Chrome", true, 0L);

        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> auditSessionService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
        verify(auditSessionValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldEndSessionAndReturnDto() {
        UUID id = UUID.randomUUID();
        Instant loginTime = Instant.now().minusSeconds(3600);
        Instant logoutTime = Instant.now();
        AuditSession entity = auditSession(id);
        entity.setLoginTime(loginTime);
        AuditSessionDto expected = dto(entity);

        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(auditSessionRepository.save(entity)).thenReturn(entity);
        when(auditSessionMapper.toDto(entity)).thenReturn(expected);

        assertThat(auditSessionService.endSession(id, logoutTime)).isEqualTo(expected);
        verify(auditSessionValidator).validateEndSession(loginTime, logoutTime);
        assertThat(entity.getLogoutTime()).isEqualTo(logoutTime);
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        AuditSession entity = auditSession(id);

        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        auditSessionService.softDelete(id);

        ArgumentCaptor<AuditSession> captor = ArgumentCaptor.forClass(AuditSession.class);
        verify(auditSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenSoftDeleteNotFound() {
        UUID id = UUID.randomUUID();
        when(auditSessionRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditSessionService.softDelete(id))
                .isInstanceOf(AuditSessionNotFoundException.class);
        verify(auditSessionRepository, never()).save(any());
    }

    private AuditSession auditSession(UUID id) {
        AuditSession entity = new AuditSession();
        entity.setId(id);
        entity.setCode("SES-001");
        entity.setSessionId("sess-abc-123");
        entity.setLoginTime(Instant.now());
        entity.setIpAddress("192.168.1.1");
        entity.setDevice("Desktop");
        entity.setBrowser("Chrome");
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private AuditSessionDto dto(AuditSession entity) {
        return new AuditSessionDto(
                entity.getId(),
                entity.getCode(),
                entity.getSessionId(),
                entity.getLoginTime(),
                entity.getLogoutTime(),
                entity.getIpAddress(),
                entity.getDevice(),
                entity.getBrowser(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
