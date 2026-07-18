package com.govos.org.mapper;

import com.govos.org.dto.EmployeeDto;
import com.govos.org.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "organization.id", target = "organizationId")
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "office.id", target = "officeId")
    @Mapping(source = "designation.id", target = "designationId")
    EmployeeDto toDto(Employee entity);
}
