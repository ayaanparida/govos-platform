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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
    }

    @Override
    public UserDto getById(UUID id) {
        return userMapper.toDto(findActiveById(id));
    }

    @Override
    public UserDto getByUsername(String username) {
        return userMapper.toDto(findActiveByUsername(username));
    }

    @Override
    public UserDto getByEmail(String email) {
        return userMapper.toDto(findActiveByEmail(email));
    }

    @Override
    @Transactional
    public UserDto create(CreateUserRequest request) {
        userValidator.validateCreate(request);

        User entity = userMapper.toEntity(request);
        applyDefaults(entity, request.active(), request.status());

        return userMapper.toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public UserDto update(UUID id, UpdateUserRequest request) {
        User entity = findActiveById(id);
        assertVersion(entity, request.version());

        userValidator.validateUpdate(id, request);
        userMapper.updateEntity(request, entity);

        if (request.passwordHash() != null && !request.passwordHash().isBlank()) {
            entity.setPasswordHash(request.passwordHash());
        }
        if (request.status() != null) {
            entity.setStatus(request.status());
        }
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (request.accountLocked() != null) {
            entity.setAccountLocked(request.accountLocked());
        }

        return userMapper.toDto(userRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        User entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        userRepository.save(entity);
    }

    @Override
    @Transactional
    public void lockAccount(UUID id) {
        User entity = findActiveById(id);
        entity.setAccountLocked(true);
        userRepository.save(entity);
    }

    @Override
    @Transactional
    public void unlockAccount(UUID id) {
        User entity = findActiveById(id);
        entity.setAccountLocked(false);
        entity.setFailedLoginAttempts(0);
        userRepository.save(entity);
    }

    @Override
    @Transactional
    public void recordFailedLogin(UUID id) {
        User entity = findActiveById(id);
        int attempts = entity.getFailedLoginAttempts() == null ? 0 : entity.getFailedLoginAttempts();
        entity.setFailedLoginAttempts(attempts + 1);
        userRepository.save(entity);
    }

    @Override
    @Transactional
    public void resetFailedLoginAttempts(UUID id) {
        User entity = findActiveById(id);
        entity.setFailedLoginAttempts(0);
        userRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID id, Instant lastLogin) {
        User entity = findActiveById(id);
        entity.setLastLogin(lastLogin);
        userRepository.save(entity);
    }

    private User findActiveById(UUID id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private User findActiveByUsername(String username) {
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private User findActiveByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private void applyDefaults(User entity, Boolean active, UserStatus status) {
        if (active != null) {
            entity.setActive(active);
        } else if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (status != null) {
            entity.setStatus(status);
        } else if (entity.getStatus() == null) {
            entity.setStatus(UserStatus.PENDING);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
        if (entity.getAccountLocked() == null) {
            entity.setAccountLocked(false);
        }
        if (entity.getFailedLoginAttempts() == null) {
            entity.setFailedLoginAttempts(0);
        }
    }

    private void assertVersion(User entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "User version mismatch for id: " + entity.getId());
        }
    }
}
