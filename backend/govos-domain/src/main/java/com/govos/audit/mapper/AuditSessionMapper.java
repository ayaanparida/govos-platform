package com.govos.audit.mapper;

import com.govos.audit.dto.AuditSessionDto;
import com.govos.audit.entity.AuditSession;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditSessionMapper {

    AuditSessionDto toDto(AuditSession entity);
}
