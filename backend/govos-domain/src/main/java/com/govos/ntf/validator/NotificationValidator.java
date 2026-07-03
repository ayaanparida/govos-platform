package com.govos.ntf.validator;

import com.govos.ntf.dto.CreateNotificationRequest;
import com.govos.ntf.dto.UpdateNotificationRequest;
import com.govos.ntf.exception.DuplicateCodeException;
import com.govos.ntf.repository.NotificationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationValidator {

    private final NotificationRepository notificationRepository;

    public NotificationValidator(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void validateCreate(CreateNotificationRequest request) {
        if (notificationRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("Notification", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateNotificationRequest request) {
        notificationRepository.findByCodeAndDeletedFalse(request.code())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new DuplicateCodeException("Notification", request.code());
                });
    }
}
