package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationSubscriptionDto;
import com.govos.ntf.entity.NotificationSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationSubscriptionMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "channel.id", target = "channelId")
    NotificationSubscriptionDto toDto(NotificationSubscription entity);
}
