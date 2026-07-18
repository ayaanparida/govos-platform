package com.govos.audit.mapper;

import com.govos.audit.dto.AuditEventDto;
import com.govos.audit.entity.AuditEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditEventMapper {

    @Mapping(source = "actor.id", target = "actorId")
    @Mapping(source = "session.id", target = "sessionId")
    AuditEventDto toDto(AuditEvent entity);
}
