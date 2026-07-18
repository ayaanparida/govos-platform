package com.govos.doc.mapper;

import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.FolderResponse;
import com.govos.doc.dto.folder.FolderTreeResponse;
import com.govos.doc.dto.folder.UpdateFolderRequest;
import com.govos.doc.entity.Folder;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FolderMapper {

    @Mapping(source = "parentFolder.id", target = "parentFolderId")
    @Mapping(source = "pathMetadata.materializedPath", target = "materializedPath")
    @Mapping(source = "pathMetadata.depthLevel", target = "depthLevel")
    FolderResponse toResponse(Folder entity);

    @Mapping(source = "parentFolder.id", target = "parentFolderId")
    @Mapping(source = "pathMetadata.materializedPath", target = "materializedPath")
    @Mapping(source = "pathMetadata.depthLevel", target = "depthLevel")
    @Mapping(target = "children", expression = "java(java.util.Collections.emptyList())")
    FolderTreeResponse toTreeResponse(Folder entity);

    List<FolderResponse> toResponseList(List<Folder> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "parentFolder", ignore = true)
    @Mapping(source = "materializedPath", target = "pathMetadata.materializedPath")
    @Mapping(source = "depthLevel", target = "pathMetadata.depthLevel")
    Folder toEntity(CreateFolderRequest request);

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
    @Mapping(target = "parentFolder", ignore = true)
    @Mapping(source = "materializedPath", target = "pathMetadata.materializedPath")
    @Mapping(source = "depthLevel", target = "pathMetadata.depthLevel")
    void updateEntity(UpdateFolderRequest request, @MappingTarget Folder entity);
}
