package com.govos.idm.service;

import com.govos.idm.dto.CreateRoleRequest;
import com.govos.idm.dto.RoleDto;
import com.govos.idm.dto.UpdateRoleRequest;

import java.util.List;
import java.util.UUID;

public interface RoleService {

    RoleDto getById(UUID id);

    RoleDto getByCode(String code);

    List<RoleDto> getAll();

    RoleDto create(CreateRoleRequest request);

    RoleDto update(UUID id, UpdateRoleRequest request);

    void softDelete(UUID id);
}
