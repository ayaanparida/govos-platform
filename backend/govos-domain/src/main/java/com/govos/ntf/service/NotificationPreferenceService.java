package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationPreferenceRequest;
import com.govos.ntf.dto.NotificationPreferenceDto;
import com.govos.ntf.dto.UpdateNotificationPreferenceRequest;

import java.util.List;
import java.util.UUID;

public interface NotificationPreferenceService {

    NotificationPreferenceDto getById(UUID id);

    List<NotificationPreferenceDto> getByUserId(UUID userId);

    NotificationPreferenceDto getByUserIdAndChannelId(UUID userId, UUID channelId);

    NotificationPreferenceDto create(CreateNotificationPreferenceRequest request);

    NotificationPreferenceDto update(UUID id, UpdateNotificationPreferenceRequest request);

    void softDelete(UUID id);
}
