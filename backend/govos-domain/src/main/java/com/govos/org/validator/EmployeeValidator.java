package com.govos.org.validator;

import com.govos.org.dto.UpdateEmployeeRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmployeeValidator {

    public void validateUpdate(UUID id, UpdateEmployeeRequest request) {
        // Employee number is immutable after creation
    }
}
