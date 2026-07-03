package com.govos.ntf.mapper;

import com.govos.ntf.dto.NotificationTemplateDto;
import com.govos.ntf.entity.NotificationTemplate;
import com.govos.ntf.template.TemplateVariableJson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationTemplateMapper {

    @Mapping(source = "channel.id", target = "channelId")
    @Mapping(source = "templateVariables", target = "templateVariables", qualifiedByName = "jsonToList")
    NotificationTemplateDto toDto(NotificationTemplate entity);

    @Named("jsonToList")
    default List<String> jsonToList(String json) {
        return TemplateVariableJson.fromJson(json);
    }
}
