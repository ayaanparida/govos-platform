package com.govos.idm.service;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.entity.RefreshToken;
import com.govos.idm.entity.User;
import com.govos.idm.exception.RefreshTokenNotFoundException;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.RefreshTokenMapper;
import com.govos.idm.repository.RefreshTokenRepository;
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
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void shouldReturnDtoWhenGetByTokenFound() {
        String token = "refresh-token-abc";
        RefreshToken entity = refreshToken(UUID.randomUUID(), token);
        RefreshTokenDto dto = dto(entity);

        when(refreshTokenRepository.findByTokenAndDeletedFalseAndRevokedFalse(token))
                .thenReturn(Optional.of(entity));
        when(refreshTokenMapper.toDto(entity)).thenReturn(dto);

        assertThat(refreshTokenService.getByToken(token)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByTokenNotFound() {
        when(refreshTokenRepository.findByTokenAndDeletedFalseAndRevokedFalse("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.getByToken("missing"))
                .isInstanceOf(RefreshTokenNotFoundException.class);
    }

    @Test
    void shouldReturnListWhenGetActiveByUserId() {
        UUID userId = UUID.randomUUID();
        RefreshToken entity = refreshToken(UUID.randomUUID(), "token-1");
        RefreshTokenDto dto = dto(entity);

        when(refreshTokenRepository.findByUser_IdAndDeletedFalseAndRevokedFalse(userId))
                .thenReturn(List.of(entity));
        when(refreshTokenMapper.toDto(entity)).thenReturn(dto);

        assertThat(refreshTokenService.getActiveByUserId(userId)).containsExactly(dto);
    }

    @Test
    void shouldCreateAndReturnDto() {
        UUID userId = UUID.randomUUID();
        Instant expiry = Instant.parse("2026-12-31T23:59:59Z");
        CreateRefreshTokenRequest request = new CreateRefreshTokenRequest(
                userId, "refresh-token-abc", expiry, false, true);
        User user = user(userId);
        RefreshToken entity = refreshToken(UUID.randomUUID(), "refresh-token-abc");
        RefreshTokenDto expected = dto(entity);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.of(user));
        when(refreshTokenMapper.toEntity(request)).thenReturn(entity);
        when(refreshTokenRepository.save(entity)).thenReturn(entity);
        when(refreshTokenMapper.toDto(entity)).thenReturn(expected);

        assertThat(refreshTokenService.create(request)).isEqualTo(expected);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getRevoked()).isFalse();
        assertThat(captor.getValue().getActive()).isTrue();
        assertThat(captor.getValue().getDeleted()).isFalse();
    }

    @Test
    void shouldThrowWhenCreateUserNotFound() {
        UUID userId = UUID.randomUUID();
        CreateRefreshTokenRequest request = new CreateRefreshTokenRequest(
                userId, "token", Instant.now(), false, true);

        when(userRepository.findByIdAndDeletedFalse(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.create(request))
                .isInstanceOf(UserNotFoundException.class);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRevoke() {
        UUID id = UUID.randomUUID();
        RefreshToken entity = refreshToken(id, "token");

        when(refreshTokenRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        refreshTokenService.revoke(id);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getRevoked()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenRevokeNotFound() {
        UUID id = UUID.randomUUID();
        when(refreshTokenRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revoke(id))
                .isInstanceOf(RefreshTokenNotFoundException.class);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldRevokeByToken() {
        String token = "refresh-token-abc";
        RefreshToken entity = refreshToken(UUID.randomUUID(), token);

        when(refreshTokenRepository.findByTokenAndDeletedFalseAndRevokedFalse(token))
                .thenReturn(Optional.of(entity));

        refreshTokenService.revokeByToken(token);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getRevoked()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldThrowWhenRevokeByTokenNotFound() {
        when(refreshTokenRepository.findByTokenAndDeletedFalseAndRevokedFalse("missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.revokeByToken("missing"))
                .isInstanceOf(RefreshTokenNotFoundException.class);
        verify(refreshTokenRepository, never()).save(any());
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

    private RefreshToken refreshToken(UUID id, String token) {
        RefreshToken entity = new RefreshToken();
        entity.setId(id);
        entity.setCode("RT-001");
        entity.setUser(user(UUID.randomUUID()));
        entity.setToken(token);
        entity.setExpiry(Instant.parse("2026-12-31T23:59:59Z"));
        entity.setRevoked(false);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private RefreshTokenDto dto(RefreshToken entity) {
        return new RefreshTokenDto(
                entity.getId(),
                entity.getCode(),
                entity.getUser().getId(),
                entity.getToken(),
                entity.getExpiry(),
                entity.getRevoked(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
