package com.govos.doc.mapper;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.DocumentCategoryResponse;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.entity.DocumentCategory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentCategoryMapper {

    @Mapping(source = "parentCategory.id", target = "parentCategoryId")
    @Mapping(source = "defaultRetentionPolicy.id", target = "defaultRetentionPolicyId")
    DocumentCategoryResponse toResponse(DocumentCategory entity);

    List<DocumentCategoryResponse> toResponseList(List<DocumentCategory> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "defaultRetentionPolicy", ignore = true)
    DocumentCategory toEntity(CreateDocumentCategoryRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    @Mapping(target = "defaultRetentionPolicy", ignore = true)
    void updateEntity(UpdateDocumentCategoryRequest request, @MappingTarget DocumentCategory entity);
}
