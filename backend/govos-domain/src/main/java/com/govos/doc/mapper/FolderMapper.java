package com.govos.doc.mapper;

import com.govos.doc.dto.FolderDto;
import com.govos.doc.entity.Folder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FolderMapper {

    @Mapping(source = "parentFolder.id", target = "parentFolderId")
    @Mapping(source = "owner.id", target = "ownerId")
    FolderDto toDto(Folder entity);
}
