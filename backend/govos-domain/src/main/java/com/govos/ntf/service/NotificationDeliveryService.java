package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationDeliveryRequest;
import com.govos.ntf.dto.NotificationDeliveryDto;
import com.govos.ntf.dto.UpdateNotificationDeliveryRequest;
import com.govos.ntf.entity.DeliveryStatus;

import java.util.List;
import java.util.UUID;

public interface NotificationDeliveryService {

    NotificationDeliveryDto getById(UUID id);

    List<NotificationDeliveryDto> getByNotificationId(UUID notificationId);

    List<NotificationDeliveryDto> getByDeliveryStatus(DeliveryStatus deliveryStatus);

    NotificationDeliveryDto create(CreateNotificationDeliveryRequest request);

    NotificationDeliveryDto update(UUID id, UpdateNotificationDeliveryRequest request);

    void softDelete(UUID id);
}
