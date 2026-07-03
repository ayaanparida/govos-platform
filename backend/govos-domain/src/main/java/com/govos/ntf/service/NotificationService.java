package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationRequest;
import com.govos.ntf.dto.NotificationDto;
import com.govos.ntf.dto.UpdateNotificationRequest;
import com.govos.ntf.entity.NotificationStatus;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationDto getById(UUID id);

    NotificationDto getByCode(String code);

    List<NotificationDto> getAll();

    List<NotificationDto> getByChannelId(UUID channelId);

    List<NotificationDto> getByStatus(NotificationStatus status);

    List<NotificationDto> getByRecipient(String recipient);

    NotificationDto create(CreateNotificationRequest request);

    NotificationDto update(UUID id, UpdateNotificationRequest request);

    void softDelete(UUID id);
}
