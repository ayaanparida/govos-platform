package com.govos.doc.mapper;

import com.govos.doc.dto.DocumentAccessLogDto;
import com.govos.doc.entity.DocumentAccessLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentAccessLogMapper {

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "user.id", target = "userId")
    DocumentAccessLogDto toDto(DocumentAccessLog entity);
}
