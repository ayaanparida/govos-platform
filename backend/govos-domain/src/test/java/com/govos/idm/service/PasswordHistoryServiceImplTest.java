package com.govos.idm.service;

import com.govos.idm.dto.CreatePasswordHistoryRequest;
import com.govos.idm.dto.PasswordHistoryDto;
import com.govos.idm.entity.PasswordHistory;
import com.govos.idm.entity.User;
import com.govos.idm.exception.AssignmentNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.PasswordHistoryMapper;
import com.govos.idm.repository.PasswordHistoryRepository;
import com.govos.idm.repository.UserRepository;
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
class PasswordHistoryServiceImplTest {

    @Mock
    private PasswordHistoryRepository passwordHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHistoryMapper passwordHistoryMapper;

    @InjectMocks
    private PasswordHistoryServiceImpl passwordHistoryService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        PasswordHistory entity = passwordHistory(id);
        PasswordHistoryDto dto = dto(entity);

        when(passwordHistoryRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(passwordHistoryMapper.toDto(entity)).thenReturn(dto);

        assertThat(passwordHistoryService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(passwordHistoryRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordHistoryService.getById(id))
                .isInstanceOf(AssignmentNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByUserId() {
        UUID userId = UUID.randomUUID();
        PasswordHistory entity = passwordHistory(UUID.randomUUID());
        PasswordHistoryDto dto = dto(entity);

        when(passwordHistoryRepository.findByUser_IdOrderByChangedDateDesc(userId))
                .thenReturn(List.of(entity));
        when(passwordHistoryMapper.toDto(entity)).thenReturn(dto);

        assertThat(passwordHistoryService.getByUserId(userId)).containsExactly(dto);
    }

    @Test
    void shouldRecordAndReturnDto() {
        UUID userId = UUID.randomUUID();
        Instant changedDate = Instant.parse("2026-01-15T10:00:00Z");
        CreatePasswordHistoryRequest request = new CreatePasswordHistoryRequest(
                userId, "new-hashed-password", changedDate, true);
        User user = user(userId);
        PasswordHistory entity = passwordHistory(UUID.randomUUID());
        PasswordHistoryDto expected = dto(entity);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(passwordHistoryMapper.toEntity(request)).thenReturn(entity);
        when(passwordHistoryRepository.save(entity)).thenReturn(entity);
        when(passwordHistoryMapper.toDto(entity)).thenReturn(expected);

        assertThat(passwordHistoryService.record(request)).isEqualTo(expected);

        ArgumentCaptor<PasswordHistory> captor = ArgumentCaptor.forClass(PasswordHistory.class);
        verify(passwordHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenRecordUserNotFound() {
        UUID userId = UUID.randomUUID();
        CreatePasswordHistoryRequest request = new CreatePasswordHistoryRequest(
                userId, "hashed-password", Instant.now(), true);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordHistoryService.record(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(passwordHistoryRepository, never()).save(any());
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

    private PasswordHistory passwordHistory(UUID id) {
        PasswordHistory entity = new PasswordHistory();
        entity.setId(id);
        entity.setCode("PH-001");
        entity.setUser(user(UUID.randomUUID()));
        entity.setPasswordHash("hashed-password");
        entity.setChangedDate(Instant.parse("2026-01-15T10:00:00Z"));
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private PasswordHistoryDto dto(PasswordHistory entity) {
        return new PasswordHistoryDto(
                entity.getId(),
                entity.getCode(),
                entity.getUser().getId(),
                entity.getPasswordHash(),
                entity.getChangedDate(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
