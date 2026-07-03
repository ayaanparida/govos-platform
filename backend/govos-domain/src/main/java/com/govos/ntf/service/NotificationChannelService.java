package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationChannelRequest;
import com.govos.ntf.dto.NotificationChannelDto;
import com.govos.ntf.dto.UpdateNotificationChannelRequest;
import com.govos.ntf.entity.ChannelProvider;

import java.util.List;
import java.util.UUID;

public interface NotificationChannelService {

    NotificationChannelDto getById(UUID id);

    NotificationChannelDto getByCode(String code);

    List<NotificationChannelDto> getAll();

    List<NotificationChannelDto> getByProvider(ChannelProvider provider);

    NotificationChannelDto create(CreateNotificationChannelRequest request);

    NotificationChannelDto update(UUID id, UpdateNotificationChannelRequest request);

    void softDelete(UUID id);
}
