package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintStatusHistoryDto;
import com.govos.cmp.entity.ComplaintStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ComplaintStatusHistoryMapper {

    @Mapping(source = "complaint.id", target = "complaintId")
    ComplaintStatusHistoryDto toDto(ComplaintStatusHistory entity);
}
