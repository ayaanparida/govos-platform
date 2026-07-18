package com.govos.doc.mapper;

import com.govos.doc.dto.audit.DocumentAccessLogResponse;
import com.govos.doc.entity.DocumentAccessLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentAccessLogMapper {

    @Mapping(source = "document.id", target = "documentId")
    DocumentAccessLogResponse toResponse(DocumentAccessLog entity);

    List<DocumentAccessLogResponse> toResponseList(List<DocumentAccessLog> entities);
}
