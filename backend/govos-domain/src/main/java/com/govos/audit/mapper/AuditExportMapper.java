package com.govos.audit.mapper;

import com.govos.audit.dto.AuditExportDto;
import com.govos.audit.entity.AuditExport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditExportMapper {

    @Mapping(source = "requestedBy.id", target = "requestedById")
    AuditExportDto toDto(AuditExport entity);
}
