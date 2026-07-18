package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationSubscriptionRequest;
import com.govos.ntf.dto.NotificationSubscriptionDto;
import com.govos.ntf.dto.UpdateNotificationSubscriptionRequest;

import java.util.List;
import java.util.UUID;

public interface NotificationSubscriptionService {

    NotificationSubscriptionDto getById(UUID id);

    List<NotificationSubscriptionDto> getByUserId(UUID userId);

    List<NotificationSubscriptionDto> getByEventType(String eventType);

    NotificationSubscriptionDto create(CreateNotificationSubscriptionRequest request);

    NotificationSubscriptionDto update(UUID id, UpdateNotificationSubscriptionRequest request);

    void softDelete(UUID id);
}
