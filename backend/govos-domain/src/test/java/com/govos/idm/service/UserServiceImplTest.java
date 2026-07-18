package com.govos.idm.service;

import com.govos.idm.dto.CreateUserRequest;
import com.govos.idm.dto.UpdateUserRequest;
import com.govos.idm.dto.UserDto;
import com.govos.idm.entity.User;
import com.govos.idm.entity.UserStatus;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.mapper.UserMapper;
import com.govos.idm.repository.UserRepository;
import com.govos.idm.validator.UserValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldReturnDtoWhenGetByIdFound() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        UserDto dto = dto(entity);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);

        assertThat(userService.getById(id)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByIdNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByUsernameFound() {
        User entity = user(UUID.randomUUID(), "jdoe", "john@example.com");
        UserDto dto = dto(entity);

        when(userRepository.findByUsernameAndDeletedFalse("jdoe")).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);

        assertThat(userService.getByUsername("jdoe")).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByUsernameNotFound() {
        when(userRepository.findByUsernameAndDeletedFalse("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldReturnDtoWhenGetByEmailFound() {
        User entity = user(UUID.randomUUID(), "jdoe", "john@example.com");
        UserDto dto = dto(entity);

        when(userRepository.findByEmailAndDeletedFalse("john@example.com")).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);

        assertThat(userService.getByEmail("john@example.com")).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenGetByEmailNotFound() {
        when(userRepository.findByEmailAndDeletedFalse("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("missing@example.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldCreateAndReturnDto() {
        CreateUserRequest request = new CreateUserRequest(
                "USR-001", "jdoe", "john@example.com", "+1234567890",
                "hashed-password", "John", null, "Doe", "M",
                LocalDate.of(1990, 1, 1), UserStatus.ACTIVE, true);
        User entity = user(UUID.randomUUID(), "jdoe", "john@example.com");
        UserDto expected = dto(entity);

        when(userMapper.toEntity(request)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDto(entity)).thenReturn(expected);

        assertThat(userService.create(request)).isEqualTo(expected);
        verify(userValidator).validateCreate(request);
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getAccountLocked()).isFalse();
        assertThat(entity.getFailedLoginAttempts()).isZero();
    }

    @Test
    void shouldUpdateAndReturnDto() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        entity.setVersion(0L);
        UpdateUserRequest request = new UpdateUserRequest(
                "USR-001", "jdoe", "john@example.com", "+1234567890",
                "new-hash", "John", null, "Doe", "M",
                LocalDate.of(1990, 1, 1), UserStatus.ACTIVE, false, true, 0L);
        UserDto expected = dto(entity);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDto(entity)).thenReturn(expected);

        assertThat(userService.update(id, request)).isEqualTo(expected);
        verify(userValidator).validateUpdate(id, request);
        verify(userMapper).updateEntity(request, entity);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        entity.setVersion(1L);
        UpdateUserRequest request = new UpdateUserRequest(
                "USR-001", "jdoe", "john@example.com", null,
                null, "John", null, "Doe", null,
                null, null, null, true, 0L);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> userService.update(id, request))
                .isInstanceOf(OptimisticLockException.class);
        verify(userValidator, never()).validateUpdate(eq(id), any());
    }

    @Test
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userService.softDelete(id);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getDeleted()).isTrue();
        assertThat(captor.getValue().getActive()).isFalse();
    }

    @Test
    void shouldLockAccount() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        entity.setAccountLocked(false);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userService.lockAccount(id);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountLocked()).isTrue();
    }

    @Test
    void shouldUnlockAccount() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        entity.setAccountLocked(true);
        entity.setFailedLoginAttempts(5);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userService.unlockAccount(id);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountLocked()).isFalse();
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
    }

    @Test
    void shouldRecordFailedLogin() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        entity.setFailedLoginAttempts(2);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userService.recordFailedLogin(id);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isEqualTo(3);
    }

    @Test
    void shouldResetFailedLoginAttempts() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        entity.setFailedLoginAttempts(5);

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userService.resetFailedLoginAttempts(id);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getFailedLoginAttempts()).isZero();
    }

    @Test
    void shouldUpdateLastLogin() {
        UUID id = UUID.randomUUID();
        User entity = user(id, "jdoe", "john@example.com");
        Instant lastLogin = Instant.parse("2026-01-15T10:00:00Z");

        when(userRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));

        userService.updateLastLogin(id, lastLogin);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getLastLogin()).isEqualTo(lastLogin);
    }

    private User user(UUID id, String username, String email) {
        User entity = new User();
        entity.setId(id);
        entity.setCode("USR-001");
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setPasswordHash("hashed-password");
        entity.setFirstName("John");
        entity.setLastName("Doe");
        entity.setStatus(UserStatus.ACTIVE);
        entity.setAccountLocked(false);
        entity.setFailedLoginAttempts(0);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    private UserDto dto(User entity) {
        return new UserDto(
                entity.getId(),
                entity.getCode(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getMobileNumber(),
                entity.getFirstName(),
                entity.getMiddleName(),
                entity.getLastName(),
                entity.getGender(),
                entity.getDateOfBirth(),
                entity.getStatus(),
                entity.getAccountLocked(),
                entity.getFailedLoginAttempts(),
                entity.getLastLogin(),
                entity.getActive(),
                entity.getVersion(),
                "system",
                Instant.now(),
                "system",
                Instant.now());
    }
}
