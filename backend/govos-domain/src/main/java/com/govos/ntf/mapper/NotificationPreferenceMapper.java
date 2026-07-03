package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationPreferenceDto;
import com.govos.ntf.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationPreferenceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "channel.id", target = "channelId")
    NotificationPreferenceDto toDto(NotificationPreference entity);
}
