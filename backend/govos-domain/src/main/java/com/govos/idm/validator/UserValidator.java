package com.govos.idm.validator;

import com.govos.idm.dto.CreateUserRequest;
import com.govos.idm.dto.UpdateUserRequest;
import com.govos.idm.exception.DuplicateEmailException;
import com.govos.idm.exception.DuplicateUsernameException;
import com.govos.idm.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateCreate(CreateUserRequest request) {
        if (userRepository.existsByUsernameAndDeletedFalse(request.username())) {
            throw new DuplicateUsernameException(request.username());
        }
        if (userRepository.existsByEmailAndDeletedFalse(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
    }

    public void validateUpdate(UUID id, UpdateUserRequest request) {
        userRepository.findByUsernameAndDeletedFalse(request.username())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new DuplicateUsernameException(request.username());
                });
        userRepository.findByEmailAndDeletedFalse(request.email())
                .filter(user -> !user.getId().equals(id))
                .ifPresent(user -> {
                    throw new DuplicateEmailException(request.email());
                });
    }
}
