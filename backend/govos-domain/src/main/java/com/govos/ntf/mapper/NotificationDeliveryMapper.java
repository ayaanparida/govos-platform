package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationDeliveryDto;
import com.govos.ntf.entity.NotificationDelivery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationDeliveryMapper {

    @Mapping(source = "notification.id", target = "notificationId")
    NotificationDeliveryDto toDto(NotificationDelivery entity);
}
