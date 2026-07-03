package com.govos.idm.validator;

import com.govos.idm.dto.CreateRoleRequest;
import com.govos.idm.dto.UpdateRoleRequest;
import com.govos.idm.exception.DuplicateCodeException;
import com.govos.idm.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoleValidator {

    private final RoleRepository roleRepository;

    public RoleValidator(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void validateCreate(CreateRoleRequest request) {
        if (roleRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Role", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateRoleRequest request) {
        roleRepository.findByCodeAndDeletedFalse(request.code())
                .filter(role -> !role.getId().equals(id))
                .ifPresent(role -> {
                    throw new DuplicateCodeException("Role", request.code());
                });
    }
}
