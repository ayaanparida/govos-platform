package com.govos.org.validator;

import com.govos.org.dto.CreateDepartmentRequest;
import com.govos.org.dto.UpdateDepartmentRequest;
import com.govos.org.exception.DuplicateCodeException;
import com.govos.org.repository.DepartmentRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DepartmentValidator {

    private final DepartmentRepository departmentRepository;

    public DepartmentValidator(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public void validateCreate(CreateDepartmentRequest request) {
        if (departmentRepository.existsByOrganization_IdAndCodeAndDeletedFalse(
                request.organizationId(), request.code())) {
            throw new DuplicateCodeException("Department", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateDepartmentRequest request) {
        departmentRepository.findByOrganization_IdAndCodeAndDeletedFalse(
                        request.organizationId(), request.code())
                .filter(dept -> !dept.getId().equals(id))
                .ifPresent(dept -> {
                    throw new DuplicateCodeException("Department", request.code());
                });
    }
}
