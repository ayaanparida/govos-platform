package com.govos.org.service;

import com.govos.org.dto.CreateEmployeeRequest;
import com.govos.org.dto.EmployeeDto;
import com.govos.org.dto.UpdateEmployeeRequest;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {

    EmployeeDto getById(UUID id);

    EmployeeDto getByEmployeeNumber(String employeeNumber);

    List<EmployeeDto> getByOrganizationId(UUID organizationId);

    List<EmployeeDto> getByUserId(UUID userId);

    EmployeeDto create(CreateEmployeeRequest request);

    EmployeeDto update(UUID id, UpdateEmployeeRequest request);

    void softDelete(UUID id);
}
