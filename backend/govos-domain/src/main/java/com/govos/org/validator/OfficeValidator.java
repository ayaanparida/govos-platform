package com.govos.org.validator;

import com.govos.org.dto.CreateOfficeRequest;
import com.govos.org.dto.UpdateOfficeRequest;
import com.govos.org.exception.DuplicateCodeException;
import com.govos.org.repository.OfficeRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OfficeValidator {

    private final OfficeRepository officeRepository;

    public OfficeValidator(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    public void validateCreate(CreateOfficeRequest request) {
        if (officeRepository.existsByDepartment_IdAndCodeAndDeletedFalse(
                request.departmentId(), request.code())) {
            throw new DuplicateCodeException("Office", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateOfficeRequest request) {
        officeRepository.findByDepartment_IdAndCodeAndDeletedFalse(
                        request.departmentId(), request.code())
                .filter(office -> !office.getId().equals(id))
                .ifPresent(office -> {
                    throw new DuplicateCodeException("Office", request.code());
                });
    }
}
