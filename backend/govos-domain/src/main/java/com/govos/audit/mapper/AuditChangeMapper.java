package com.govos.audit.mapper;

import com.govos.audit.dto.AuditChangeDto;
import com.govos.audit.entity.AuditChange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditChangeMapper {

    @Mapping(source = "auditEvent.id", target = "auditEventId")
    AuditChangeDto toDto(AuditChange entity);
}
