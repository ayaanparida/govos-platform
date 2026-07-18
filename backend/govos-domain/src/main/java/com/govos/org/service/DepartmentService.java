package com.govos.org.service;

import com.govos.org.dto.CreateDepartmentRequest;
import com.govos.org.dto.DepartmentDto;
import com.govos.org.dto.UpdateDepartmentRequest;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentDto getById(UUID id);

    List<DepartmentDto> getByOrganizationId(UUID organizationId);

    List<DepartmentDto> getByParentDepartmentId(UUID parentDepartmentId);

    DepartmentDto create(CreateDepartmentRequest request);

    DepartmentDto update(UUID id, UpdateDepartmentRequest request);

    void softDelete(UUID id);
}
