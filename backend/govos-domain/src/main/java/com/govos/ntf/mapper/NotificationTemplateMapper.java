package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationTemplateDto;
import com.govos.ntf.entity.NotificationTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationTemplateMapper {

    @Mapping(source = "channel.id", target = "channelId")
    NotificationTemplateDto toDto(NotificationTemplate entity);
}
