package com.govos.ntf.validator;

import com.govos.ntf.dto.CreateNotificationPreferenceRequest;
import com.govos.ntf.exception.DuplicateAssignmentException;
import com.govos.ntf.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Component;

@Component
public class NotificationPreferenceValidator {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceValidator(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    public void validateCreate(CreateNotificationPreferenceRequest request) {
        if (notificationPreferenceRepository.existsByUser_IdAndChannel_IdAndDeletedFalse(
                request.userId(), request.channelId())) {
            throw new DuplicateAssignmentException(
                    "Notification preference already exists for user=" + request.userId()
                            + ", channel=" + request.channelId());
        }
    }
}
