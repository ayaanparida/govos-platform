package com.govos.org.service;

import com.govos.org.dto.CreateDesignationRequest;
import com.govos.org.dto.DesignationDto;
import com.govos.org.dto.UpdateDesignationRequest;

import java.util.List;
import java.util.UUID;

public interface DesignationService {

    DesignationDto getById(UUID id);

    DesignationDto getByCode(String code);

    List<DesignationDto> getAll();

    DesignationDto create(CreateDesignationRequest request);

    DesignationDto update(UUID id, UpdateDesignationRequest request);

    void softDelete(UUID id);
}
