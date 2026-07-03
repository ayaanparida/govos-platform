package com.govos.org.validator;

import com.govos.org.dto.CreateEmployeeRequest;
import com.govos.org.dto.UpdateEmployeeRequest;
import com.govos.org.exception.DuplicateEmployeeNumberException;
import com.govos.org.repository.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeValidator {

    private final EmployeeRepository employeeRepository;

    public EmployeeValidator(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void validateCreate(CreateEmployeeRequest request) {
        if (employeeRepository.existsByEmployeeNumberAndDeletedFalse(request.employeeNumber())) {
            throw new DuplicateEmployeeNumberException(request.employeeNumber());
        }
    }

    public void validateUpdate(UUID id, UpdateEmployeeRequest request) {
        employeeRepository.findByEmployeeNumberAndDeletedFalse(request.employeeNumber())
                .filter(employee -> !employee.getId().equals(id))
                .ifPresent(employee -> {
                    throw new DuplicateEmployeeNumberException(request.employeeNumber());
                });
    }
}
