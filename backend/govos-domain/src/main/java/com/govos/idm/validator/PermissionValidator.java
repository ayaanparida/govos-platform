package com.govos.idm.validator;

import com.govos.idm.dto.CreatePermissionRequest;
import com.govos.idm.dto.UpdatePermissionRequest;
import com.govos.idm.exception.DuplicateCodeException;
import com.govos.idm.repository.PermissionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PermissionValidator {

    private final PermissionRepository permissionRepository;

    public PermissionValidator(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public void validateCreate(CreatePermissionRequest request) {
        if (permissionRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Permission", request.code());
        }
        if (permissionRepository.existsByModuleAndResourceAndActionAndDeletedFalse(
                request.module(), request.resource(), request.action())) {
            throw new DuplicateCodeException("Permission",
                    request.module() + ":" + request.resource() + ":" + request.action());
        }
    }

    public void validateUpdate(UUID id, UpdatePermissionRequest request) {
        permissionRepository.findByCodeAndDeletedFalse(request.code())
                .filter(permission -> !permission.getId().equals(id))
                .ifPresent(permission -> {
                    throw new DuplicateCodeException("Permission", request.code());
                });
    }
}
