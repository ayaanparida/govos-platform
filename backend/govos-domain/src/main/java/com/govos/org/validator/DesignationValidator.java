package com.govos.org.validator;

import com.govos.org.dto.CreateDesignationRequest;
import com.govos.org.dto.UpdateDesignationRequest;
import com.govos.org.exception.DuplicateCodeException;
import com.govos.org.repository.DesignationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DesignationValidator {

    private final DesignationRepository designationRepository;

    public DesignationValidator(DesignationRepository designationRepository) {
        this.designationRepository = designationRepository;
    }

    public void validateCreate(CreateDesignationRequest request) {
        if (designationRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Designation", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateDesignationRequest request) {
        designationRepository.findByCodeAndDeletedFalse(request.code())
                .filter(designation -> !designation.getId().equals(id))
                .ifPresent(designation -> {
                    throw new DuplicateCodeException("Designation", request.code());
                });
    }
}
