package com.govos.org.service;

import com.govos.org.dto.CreateOrganizationRequest;
import com.govos.org.dto.OrganizationDto;
import com.govos.org.dto.UpdateOrganizationRequest;

import java.util.List;
import java.util.UUID;

public interface OrganizationService {

    OrganizationDto getById(UUID id);

    OrganizationDto getByCode(String code);

    List<OrganizationDto> getAll();

    OrganizationDto create(CreateOrganizationRequest request);

    OrganizationDto update(UUID id, UpdateOrganizationRequest request);

    void softDelete(UUID id);
}
