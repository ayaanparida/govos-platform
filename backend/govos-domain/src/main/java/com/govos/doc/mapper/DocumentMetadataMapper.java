package com.govos.doc.mapper;

import com.govos.doc.dto.metadata.DocumentMetadataResponse;
import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.entity.DocumentMetadata;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMetadataMapper {

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "documentVersion.id", target = "documentVersionId")
    DocumentMetadataResponse toResponse(DocumentMetadata entity);

    List<DocumentMetadataResponse> toResponseList(List<DocumentMetadata> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "documentVersion", ignore = true)
    void updateEntity(UpdateDocumentMetadataRequest request, @MappingTarget DocumentMetadata entity);
}
