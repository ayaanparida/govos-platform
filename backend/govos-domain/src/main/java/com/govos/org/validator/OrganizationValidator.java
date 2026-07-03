package com.govos.org.validator;

import com.govos.org.dto.CreateOrganizationRequest;
import com.govos.org.dto.UpdateOrganizationRequest;
import com.govos.org.exception.DuplicateCodeException;
import com.govos.org.repository.OrganizationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrganizationValidator {

    private final OrganizationRepository organizationRepository;

    public OrganizationValidator(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public void validateCreate(CreateOrganizationRequest request) {
        if (organizationRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Organization", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateOrganizationRequest request) {
        organizationRepository.findByCodeAndDeletedFalse(request.code())
                .filter(org -> !org.getId().equals(id))
                .ifPresent(org -> {
                    throw new DuplicateCodeException("Organization", request.code());
                });
    }
}
