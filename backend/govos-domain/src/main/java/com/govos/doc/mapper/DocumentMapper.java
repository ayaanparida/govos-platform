package com.govos.doc.mapper;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.DocumentListResponse;
import com.govos.doc.dto.document.DocumentReferenceDto;
import com.govos.doc.dto.document.DocumentResponse;
import com.govos.doc.dto.document.DocumentSearchResponse;
import com.govos.doc.dto.document.DocumentSummaryResponse;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(source = "folder.id", target = "folderId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "retentionPolicy.id", target = "retentionPolicyId")
    @Mapping(source = "activeVersion.id", target = "activeVersionId")
    DocumentResponse toResponse(Document entity);

    @Mapping(source = "folder.id", target = "folderId")
    @Mapping(source = "category.id", target = "categoryId")
    DocumentSearchResponse toSearchResponse(Document entity);

    DocumentSummaryResponse toSummaryResponse(Document entity);

    DocumentReferenceDto toReferenceDto(Document entity);

    List<DocumentSummaryResponse> toSummaryResponseList(List<Document> entities);

    default DocumentListResponse toListResponse(Page<Document> page) {
        return new DocumentListResponse(
                toSummaryResponseList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "folder", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "retentionPolicy", ignore = true)
    @Mapping(target = "activeVersion", ignore = true)
    @Mapping(target = "versions", ignore = true)
    @Mapping(target = "metadataEntries", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    Document toEntity(CreateDocumentRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "folder", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "retentionPolicy", ignore = true)
    @Mapping(target = "activeVersion", ignore = true)
    @Mapping(target = "versions", ignore = true)
    @Mapping(target = "metadataEntries", ignore = true)
    @Mapping(target = "shares", ignore = true)
    @Mapping(target = "accessLogs", ignore = true)
    void updateEntity(UpdateDocumentRequest request, @MappingTarget Document entity);
}
