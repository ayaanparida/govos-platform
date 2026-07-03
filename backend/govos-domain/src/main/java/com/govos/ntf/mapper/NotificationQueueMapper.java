package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationQueueDto;
import com.govos.ntf.entity.NotificationQueue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationQueueMapper {

    @Mapping(source = "notification.id", target = "notificationId")
    NotificationQueueDto toDto(NotificationQueue entity);
}
