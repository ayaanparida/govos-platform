package com.govos.ntf.mapper;

import com.govos.ntf.dto.CreateNotificationChannelRequest;
import com.govos.ntf.dto.NotificationChannelDto;
import com.govos.ntf.dto.UpdateNotificationChannelRequest;
import com.govos.ntf.entity.NotificationChannel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationChannelMapper {

    NotificationChannelDto toDto(NotificationChannel entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    NotificationChannel toEntity(CreateNotificationChannelRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    void updateEntity(UpdateNotificationChannelRequest request, @MappingTarget NotificationChannel entity);
}
