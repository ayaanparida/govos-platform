package com.govos.idm.service;

import com.govos.idm.dto.CreatePermissionRequest;
import com.govos.idm.dto.PermissionDto;
import com.govos.idm.dto.UpdatePermissionRequest;

import java.util.List;
import java.util.UUID;

public interface PermissionService {

    PermissionDto getById(UUID id);

    PermissionDto getByCode(String code);

    List<PermissionDto> getByModule(String module);

    PermissionDto create(CreatePermissionRequest request);

    PermissionDto update(UUID id, UpdatePermissionRequest request);

    void softDelete(UUID id);
}
