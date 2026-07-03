package com.govos.idm.mapper;

import com.govos.idm.dto.CreateRefreshTokenRequest;
import com.govos.idm.dto.RefreshTokenDto;
import com.govos.idm.entity.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenMapper {

    @Mapping(source = "user.id", target = "userId")
    RefreshTokenDto toDto(RefreshToken entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "user", ignore = true)
    RefreshToken toEntity(CreateRefreshTokenRequest request);
}
