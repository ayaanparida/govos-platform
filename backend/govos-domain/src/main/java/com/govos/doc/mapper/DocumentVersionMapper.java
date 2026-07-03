package com.govos.doc.mapper;

import com.govos.doc.dto.DocumentVersionDto;
import com.govos.doc.entity.DocumentVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentVersionMapper {

    @Mapping(source = "document.id", target = "documentId")
    DocumentVersionDto toDto(DocumentVersion entity);
}
