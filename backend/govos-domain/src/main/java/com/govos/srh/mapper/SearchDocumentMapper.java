package com.govos.srh.mapper;

import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.entity.SearchDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SearchDocumentMapper {

    @Mapping(source = "searchIndex.id", target = "searchIndexId")
    @Mapping(source = "status", target = "documentStatus")
    @Mapping(source = "metadata.organizationId", target = "metadataOrganizationId")
    @Mapping(source = "metadata.entityType", target = "metadataEntityType")
    @Mapping(source = "metadata.referenceId", target = "metadataReferenceId")
    @Mapping(source = "metadata.referenceCode", target = "metadataReferenceCode")
    @Mapping(source = "metadata.mappingVersion", target = "metadataMappingVersion")
    @Mapping(source = "metadata.indexedAt", target = "metadataIndexedAt")
    @Mapping(source = "metadata.lastIndexedAt", target = "metadataLastIndexedAt")
    SearchDocumentDto toDto(SearchDocument entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "searchIndex", ignore = true)
    @Mapping(target = "metadata.organizationId", source = "metadataOrganizationId")
    @Mapping(target = "metadata.entityType", source = "metadataEntityType")
    @Mapping(target = "metadata.referenceId", source = "metadataReferenceId")
    @Mapping(target = "metadata.referenceCode", source = "metadataReferenceCode")
    @Mapping(target = "metadata.mappingVersion", source = "metadataMappingVersion")
    @Mapping(target = "metadata.indexedAt", source = "metadataIndexedAt")
    @Mapping(target = "metadata.lastIndexedAt", source = "metadataLastIndexedAt")
    SearchDocument toEntity(SearchDocumentCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "searchDocumentId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "searchIndex", ignore = true)
    @Mapping(target = "metadata.organizationId", source = "metadataOrganizationId")
    @Mapping(target = "metadata.entityType", source = "metadataEntityType")
    @Mapping(target = "metadata.referenceId", source = "metadataReferenceId")
    @Mapping(target = "metadata.referenceCode", source = "metadataReferenceCode")
    @Mapping(target = "metadata.mappingVersion", source = "metadataMappingVersion")
    @Mapping(target = "metadata.indexedAt", source = "metadataIndexedAt")
    @Mapping(target = "metadata.lastIndexedAt", source = "metadataLastIndexedAt")
    void updateEntity(SearchDocumentUpdateRequest request, @MappingTarget SearchDocument entity);
}
