package com.govos.org.mapper;

import com.govos.org.dto.DepartmentDto;
import com.govos.org.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentMapper {

    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "parentDepartment.id", target = "parentDepartmentId")
    DepartmentDto toDto(Department entity);
}
