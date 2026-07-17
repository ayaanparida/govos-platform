package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.entity.ComplaintEscalation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComplaintEscalationMapper {

    @Mapping(source = "complaint.id", target = "complaintId")
    ComplaintEscalationDto toDto(ComplaintEscalation entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "complaint", ignore = true)
    ComplaintEscalation toEntity(ComplaintEscalationCreateRequest request);
}
