package com.govos.audit.mapper;

import com.govos.audit.dto.AuditEntityDto;
import com.govos.audit.entity.AuditEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditEntityMapper {

    AuditEntityDto toDto(AuditEntity entity);
}
