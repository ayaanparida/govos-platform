package com.govos.org.mapper;

import com.govos.org.dto.OfficeDto;
import com.govos.org.entity.Office;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OfficeMapper {

    @Mapping(source = "department.id", target = "departmentId")
    OfficeDto toDto(Office entity);
}
