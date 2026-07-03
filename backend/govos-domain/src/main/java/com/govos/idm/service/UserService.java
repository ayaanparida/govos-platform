package com.govos.idm.service;

import com.govos.idm.dto.CreateUserRequest;
import com.govos.idm.dto.UpdateUserRequest;
import com.govos.idm.dto.UserDto;

import java.time.Instant;
import java.util.UUID;

public interface UserService {

    UserDto getById(UUID id);

    UserDto getByUsername(String username);

    UserDto getByEmail(String email);

    UserDto create(CreateUserRequest request);

    UserDto update(UUID id, UpdateUserRequest request);

    void softDelete(UUID id);

    void lockAccount(UUID id);

    void unlockAccount(UUID id);

    void recordFailedLogin(UUID id);

    void resetFailedLoginAttempts(UUID id);

    void updateLastLogin(UUID id, Instant lastLogin);
}
