package com.govos.doc.mapper;

import com.govos.doc.dto.DocumentTagMappingDto;
import com.govos.doc.entity.DocumentTagMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentTagMappingMapper {

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "tag.id", target = "tagId")
    DocumentTagMappingDto toDto(DocumentTagMapping entity);
}
