package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationTemplateRequest;
import com.govos.ntf.dto.NotificationTemplateDto;
import com.govos.ntf.dto.UpdateNotificationTemplateRequest;

import java.util.List;
import java.util.UUID;

public interface NotificationTemplateService {

    NotificationTemplateDto getById(UUID id);

    NotificationTemplateDto getByCode(String code);

    List<NotificationTemplateDto> getAll();

    List<NotificationTemplateDto> getByChannelId(UUID channelId);

    NotificationTemplateDto create(CreateNotificationTemplateRequest request);

    NotificationTemplateDto update(UUID id, UpdateNotificationTemplateRequest request);

    void softDelete(UUID id);
}
