package com.govos.org.service;

import com.govos.org.dto.CreateDepartmentHierarchyRequest;
import com.govos.org.dto.DepartmentHierarchyDto;

import java.util.List;
import java.util.UUID;

public interface DepartmentHierarchyService {

    DepartmentHierarchyDto getById(UUID id);

    List<DepartmentHierarchyDto> getByParentDepartmentId(UUID parentDepartmentId);

    List<DepartmentHierarchyDto> getByChildDepartmentId(UUID childDepartmentId);

    DepartmentHierarchyDto create(CreateDepartmentHierarchyRequest request);

    void remove(UUID id);
}
