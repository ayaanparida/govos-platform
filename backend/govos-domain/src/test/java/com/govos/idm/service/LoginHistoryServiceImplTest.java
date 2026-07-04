package com.govos.idm.service;

import com.govos.idm.dto.CreateLoginHistoryRequest;
import com.govos.idm.dto.LoginHistoryDto;
import com.govos.idm.entity.LoginHistory;
import com.govos.idm.entity.User;
import com.govos.idm.exception.AssignmentNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.LoginHistoryMapper;
import com.govos.idm.repository.LoginHistoryRepository;
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
class LoginHistoryServiceImplTest {

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginHistoryMapper loginHistoryMapper;

    @InjectMocks
    private LoginHistoryServiceImpl loginHistoryService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        LoginHistory entity = loginHistory(id);
        LoginHistoryDto dto = dto(entity);

        when(loginHistoryRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(loginHistoryMapper.toDto(entity)).thenReturn(dto);

        assertThat(loginHistoryService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(loginHistoryRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginHistoryService.getById(id))
                .isInstanceOf(AssignmentNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetByUserId() {
        UUID userId = UUID.randomUUID();
        LoginHistory entity = loginHistory(UUID.randomUUID());
        LoginHistoryDto dto = dto(entity);

        when(loginHistoryRepository.findByUser_IdOrderByLoginTimeDesc(userId))
                .thenReturn(List.of(entity));
        when(loginHistoryMapper.toDto(entity)).thenReturn(dto);

        assertThat(loginHistoryService.getByUserId(userId)).containsExactly(dto);
    }

    @Test
    void shouldRecordAndReturnDto() {
        UUID userId = UUID.randomUUID();
        Instant loginTime = Instant.parse("2026-01-15T10:00:00Z");
        CreateLoginHistoryRequest request = new CreateLoginHistoryRequest(
                userId, loginTime, null, "192.168.1.1", "Desktop", "Chrome", true, true);
        User user = user(userId);
        LoginHistory entity = loginHistory(UUID.randomUUID());
        LoginHistoryDto expected = dto(entity);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(loginHistoryMapper.toEntity(request)).thenReturn(entity);
        when(loginHistoryRepository.save(entity)).thenReturn(entity);
        when(loginHistoryMapper.toDto(entity)).thenReturn(expected);

        assertThat(loginHistoryService.record(request)).isEqualTo(expected);

        ArgumentCaptor<LoginHistory> captor = ArgumentCaptor.forClass(LoginHistory.class);
        verify(loginHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenRecordUserNotFound() {
        UUID userId = UUID.randomUUID();
        CreateLoginHistoryRequest request = new CreateLoginHistoryRequest(
                userId, Instant.now(), null, null, null, null, true, true);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginHistoryService.record(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(loginHistoryRepository, never()).save(any());
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

    private LoginHistory loginHistory(UUID id) {
        LoginHistory entity = new LoginHistory();
        entity.setId(id);
        entity.setCode("LH-001");
        entity.setUser(user(UUID.randomUUID()));
        entity.setLoginTime(Instant.parse("2026-01-15T10:00:00Z"));
        entity.setIpAddress("192.168.1.1");
        entity.setDevice("Desktop");
        entity.setBrowser("Chrome");
        entity.setSuccess(true);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private LoginHistoryDto dto(LoginHistory entity) {
        return new LoginHistoryDto(
                entity.getId(),
                entity.getCode(),
                entity.getUser().getId(),
                entity.getLoginTime(),
                entity.getLogoutTime(),
                entity.getIpAddress(),
                entity.getDevice(),
                entity.getBrowser(),
                entity.getSuccess(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
