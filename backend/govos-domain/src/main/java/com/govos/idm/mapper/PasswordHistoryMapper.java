package com.govos.idm.mapper;

import com.govos.idm.dto.CreatePasswordHistoryRequest;
import com.govos.idm.dto.PasswordHistoryDto;
import com.govos.idm.entity.PasswordHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PasswordHistoryMapper {

    @Mapping(source = "user.id", target = "userId")
    PasswordHistoryDto toDto(PasswordHistory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "user", ignore = true)
    PasswordHistory toEntity(CreatePasswordHistoryRequest request);
}
