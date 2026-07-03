package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationQueueRequest;
import com.govos.ntf.dto.NotificationQueueDto;
import com.govos.ntf.dto.UpdateNotificationQueueRequest;

import java.util.List;
import java.util.UUID;

public interface NotificationQueueService {

    NotificationQueueDto getById(UUID id);

    List<NotificationQueueDto> getByNotificationId(UUID notificationId);

    NotificationQueueDto create(CreateNotificationQueueRequest request);

    NotificationQueueDto update(UUID id, UpdateNotificationQueueRequest request);

    void softDelete(UUID id);
}
