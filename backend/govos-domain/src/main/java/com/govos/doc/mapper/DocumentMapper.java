package com.govos.doc.mapper;

import com.govos.doc.dto.DocumentDto;
import com.govos.doc.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(source = "storageProvider.id", target = "storageProviderId")
    @Mapping(source = "folder.id", target = "folderId")
    @Mapping(source = "owner.id", target = "ownerId")
    DocumentDto toDto(Document entity);
}
