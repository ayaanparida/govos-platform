package com.govos.ntf.validator;

import com.govos.ntf.dto.CreateNotificationChannelRequest;
import com.govos.ntf.dto.UpdateNotificationChannelRequest;
import com.govos.ntf.exception.DuplicateCodeException;
import com.govos.ntf.repository.NotificationChannelRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationChannelValidator {

    private final NotificationChannelRepository notificationChannelRepository;

    public NotificationChannelValidator(NotificationChannelRepository notificationChannelRepository) {
        this.notificationChannelRepository = notificationChannelRepository;
    }

    public void validateCreate(CreateNotificationChannelRequest request) {
        if (notificationChannelRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("NotificationChannel", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateNotificationChannelRequest request) {
        notificationChannelRepository.findByCodeAndDeletedFalse(request.code())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new DuplicateCodeException("NotificationChannel", request.code());
                });
    }
}
