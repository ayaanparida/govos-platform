package com.govos.org.mapper;

import com.govos.org.dto.UserOrganizationDto;
import com.govos.org.entity.UserOrganization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserOrganizationMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "organization.id", target = "organizationId")
    UserOrganizationDto toDto(UserOrganization entity);
}
