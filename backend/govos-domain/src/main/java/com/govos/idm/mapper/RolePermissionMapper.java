package com.govos.idm.mapper;

import com.govos.idm.dto.RolePermissionDto;
import com.govos.idm.entity.RolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolePermissionMapper {

    @Mapping(source = "role.id", target = "roleId")
    @Mapping(source = "permission.id", target = "permissionId")
    RolePermissionDto toDto(RolePermission entity);
}
