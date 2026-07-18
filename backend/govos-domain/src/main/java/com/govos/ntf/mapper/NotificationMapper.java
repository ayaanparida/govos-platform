package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationDto;
import com.govos.ntf.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(source = "channel.id", target = "channelId")
    NotificationDto toDto(Notification entity);
}
