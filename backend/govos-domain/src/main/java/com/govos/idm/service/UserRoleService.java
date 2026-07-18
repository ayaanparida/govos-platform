package com.govos.idm.service;

import com.govos.idm.dto.AssignUserRoleRequest;
import com.govos.idm.dto.UserRoleDto;

import java.util.List;
import java.util.UUID;

public interface UserRoleService {

    UserRoleDto getById(UUID id);

    List<UserRoleDto> getByUserId(UUID userId);

    List<UserRoleDto> getByRoleId(UUID roleId);

    UserRoleDto assign(AssignUserRoleRequest request);

    void revoke(UUID id);
}
