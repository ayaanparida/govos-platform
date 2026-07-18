package com.govos.doc.mapper;

import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.dto.share.ShareLinkResponse;
import com.govos.doc.dto.share.ShareResponse;
import com.govos.doc.entity.DocumentShare;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentShareMapper {

    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "shareToken.publicLinkUrl", target = "publicLinkUrl")
    @Mapping(source = "shareToken.signedUrlExpiresAt", target = "signedUrlExpiresAt")
    ShareResponse toResponse(DocumentShare entity);

    @Mapping(source = "id", target = "shareId")
    @Mapping(source = "document.id", target = "documentId")
    @Mapping(source = "shareToken.publicLinkUrl", target = "publicLinkUrl")
    @Mapping(source = "shareToken.signedUrlExpiresAt", target = "signedUrlExpiresAt")
    ShareLinkResponse toLinkResponse(DocumentShare entity);

    List<ShareResponse> toResponseList(List<DocumentShare> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "document", ignore = true)
    @Mapping(target = "shareToken.tokenHash", ignore = true)
    @Mapping(source = "publicLinkUrl", target = "shareToken.publicLinkUrl")
    @Mapping(source = "signedUrlExpiresAt", target = "shareToken.signedUrlExpiresAt")
    DocumentShare toEntity(CreateShareRequest request);
}
