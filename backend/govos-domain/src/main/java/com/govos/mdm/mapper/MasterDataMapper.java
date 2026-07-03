package com.govos.mdm.mapper;

import com.govos.mdm.dto.CreateMasterDataRequest;
import com.govos.mdm.dto.MasterDataDto;
import com.govos.mdm.dto.UpdateMasterDataRequest;
import com.govos.mdm.entity.MasterData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MasterDataMapper {

    MasterDataDto toDto(MasterData entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    MasterData toEntity(CreateMasterDataRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void updateEntity(UpdateMasterDataRequest request, @MappingTarget MasterData entity);
}
