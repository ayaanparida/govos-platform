package com.govos.org.service;

import com.govos.org.dto.CreateOfficeRequest;
import com.govos.org.dto.OfficeDto;
import com.govos.org.dto.UpdateOfficeRequest;

import java.util.List;
import java.util.UUID;

public interface OfficeService {

    OfficeDto getById(UUID id);

    List<OfficeDto> getByDepartmentId(UUID departmentId);

    OfficeDto create(CreateOfficeRequest request);

    OfficeDto update(UUID id, UpdateOfficeRequest request);

    void softDelete(UUID id);
}
