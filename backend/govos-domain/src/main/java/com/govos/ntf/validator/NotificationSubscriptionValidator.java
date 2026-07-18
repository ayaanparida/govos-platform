package com.govos.ntf.validator;

import com.govos.ntf.dto.CreateNotificationSubscriptionRequest;
import com.govos.ntf.dto.UpdateNotificationSubscriptionRequest;
import com.govos.ntf.exception.DuplicateAssignmentException;
import com.govos.ntf.repository.NotificationSubscriptionRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationSubscriptionValidator {

    private final NotificationSubscriptionRepository notificationSubscriptionRepository;

    public NotificationSubscriptionValidator(
            NotificationSubscriptionRepository notificationSubscriptionRepository) {
        this.notificationSubscriptionRepository = notificationSubscriptionRepository;
    }

    public void validateCreate(CreateNotificationSubscriptionRequest request) {
        if (notificationSubscriptionRepository.existsByUser_IdAndEventTypeAndChannel_IdAndDeletedFalse(
                request.userId(), request.eventType(), request.channelId())) {
            throw new DuplicateAssignmentException(
                    "Notification subscription already exists for user=" + request.userId()
                            + ", eventType=" + request.eventType()
                            + ", channel=" + request.channelId());
        }
    }

    public void validateUpdate(UUID id, UUID userId, UpdateNotificationSubscriptionRequest request) {
        notificationSubscriptionRepository
                .findByUser_IdAndDeletedFalse(userId).stream()
                .filter(sub -> sub.getEventType().equals(request.eventType())
                        && sub.getChannel().getId().equals(request.channelId())
                        && !sub.getId().equals(id))
                .findAny()
                .ifPresent(sub -> {
                    throw new DuplicateAssignmentException(
                            "Notification subscription already exists for user=" + userId
                                    + ", eventType=" + request.eventType()
                                    + ", channel=" + request.channelId());
                });
    }
}
