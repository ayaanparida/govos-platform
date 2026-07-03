package com.govos.org.service;

import com.govos.org.dto.AssignUserOrganizationRequest;
import com.govos.org.dto.UserOrganizationDto;

import java.util.List;
import java.util.UUID;

public interface UserOrganizationService {

    UserOrganizationDto getById(UUID id);

    List<UserOrganizationDto> getByUserId(UUID userId);

    UserOrganizationDto getDefaultByUserId(UUID userId);

    UserOrganizationDto assign(AssignUserOrganizationRequest request);

    void revoke(UUID id);

    UserOrganizationDto setDefault(UUID id);
}
