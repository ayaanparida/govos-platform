package com.govos.audit.mapper;

import com.govos.audit.dto.AuditActorDto;
import com.govos.audit.entity.AuditActor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuditActorMapper {

    @Mapping(source = "user.id", target = "userId")
    AuditActorDto toDto(AuditActor entity);
}
