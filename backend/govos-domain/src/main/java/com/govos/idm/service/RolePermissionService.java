package com.govos.idm.service;

import com.govos.idm.dto.AssignRolePermissionRequest;
import com.govos.idm.dto.RolePermissionDto;

import java.util.List;
import java.util.UUID;

public interface RolePermissionService {

    RolePermissionDto getById(UUID id);

    List<RolePermissionDto> getByRoleId(UUID roleId);

    List<RolePermissionDto> getByPermissionId(UUID permissionId);

    RolePermissionDto assign(AssignRolePermissionRequest request);

    void revoke(UUID id);
}
