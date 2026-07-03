package com.govos.org.mapper;

import com.govos.org.dto.DepartmentHierarchyDto;
import com.govos.org.entity.DepartmentHierarchy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DepartmentHierarchyMapper {

    @Mapping(source = "parentDepartment.id", target = "parentDepartmentId")
    @Mapping(source = "childDepartment.id", target = "childDepartmentId")
    DepartmentHierarchyDto toDto(DepartmentHierarchy entity);
}
