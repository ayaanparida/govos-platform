package com.govos.doc.mapper;

import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.RetentionPolicyResponse;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.entity.DocumentRetentionPolicy;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentRetentionPolicyMapper {

    RetentionPolicyResponse toResponse(DocumentRetentionPolicy entity);

    List<RetentionPolicyResponse> toResponseList(List<DocumentRetentionPolicy> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    DocumentRetentionPolicy toEntity(CreateRetentionPolicyRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "organizationId", ignore = true)
    void updateEntity(UpdateRetentionPolicyRequest request, @MappingTarget DocumentRetentionPolicy entity);
}
