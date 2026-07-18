package com.govos.doc.mapper;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.dto.version.DocumentVersionResponse;
import com.govos.doc.dto.version.DocumentVersionSummaryResponse;
import com.govos.doc.dto.version.UpdateDocumentVersionRequest;
import com.govos.doc.entity.DocumentVersion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentVersionMapper {

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "storageProvider.id", target = "storageProviderId")
    @Mapping(source = "checksum.value", target = "checksum")
    @Mapping(source = "versionNumber.value", target = "versionNumber")
    @Mapping(source = "versionNumber.label", target = "versionLabel")
    @Mapping(source = "fileSize.sizeBytes", target = "sizeBytes")
    @Mapping(source = "storageLocation.storageObjectKey", target = "storageObjectKey")
    @Mapping(source = "storageLocation.previewStorageKey", target = "previewStorageKey")
    @Mapping(source = "storageLocation.thumbnailStorageKey", target = "thumbnailStorageKey")
    DocumentVersionResponse toResponse(DocumentVersion entity);

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "versionNumber.value", target = "versionNumber")
    @Mapping(source = "versionNumber.label", target = "versionLabel")
    @Mapping(source = "fileSize.sizeBytes", target = "sizeBytes")
    DocumentVersionSummaryResponse toSummaryResponse(DocumentVersion entity);

    List<DocumentVersionResponse> toResponseList(List<DocumentVersion> entities);

    List<DocumentVersionSummaryResponse> toSummaryResponseList(List<DocumentVersion> entities);

    default List<DocumentVersionSummaryResponse> toSummaryResponsePage(Page<DocumentVersion> page) {
        return toSummaryResponseList(page.getContent());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "storageProvider", ignore = true)
    @Mapping(target = "immutable", ignore = true)
    @Mapping(source = "checksum", target = "checksum.value")
    @Mapping(source = "versionNumber", target = "versionNumber.value")
    @Mapping(source = "versionLabel", target = "versionNumber.label")
    @Mapping(source = "sizeBytes", target = "fileSize.sizeBytes")
    @Mapping(source = "storageObjectKey", target = "storageLocation.storageObjectKey")
    @Mapping(source = "previewStorageKey", target = "storageLocation.previewStorageKey")
    @Mapping(source = "thumbnailStorageKey", target = "storageLocation.thumbnailStorageKey")
    DocumentVersion toEntity(CreateDocumentVersionRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "storageProvider", ignore = true)
    @Mapping(target = "checksum", ignore = true)
    @Mapping(target = "versionNumber", ignore = true)
    @Mapping(target = "fileSize", ignore = true)
    @Mapping(target = "storageLocation.storageObjectKey", ignore = true)
    @Mapping(target = "mimeType", ignore = true)
    @Mapping(target = "originalFilename", ignore = true)
    @Mapping(target = "uploadedById", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    @Mapping(target = "immutable", ignore = true)
    @Mapping(source = "versionLabel", target = "versionNumber.label")
    @Mapping(source = "previewStorageKey", target = "storageLocation.previewStorageKey")
    @Mapping(source = "thumbnailStorageKey", target = "storageLocation.thumbnailStorageKey")
    void updateEntity(UpdateDocumentVersionRequest request, @MappingTarget DocumentVersion entity);
}
