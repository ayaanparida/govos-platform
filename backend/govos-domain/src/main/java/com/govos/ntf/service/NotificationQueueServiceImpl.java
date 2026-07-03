package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationQueueRequest;
import com.govos.ntf.dto.NotificationQueueDto;
import com.govos.ntf.dto.UpdateNotificationQueueRequest;
import com.govos.ntf.entity.Notification;
import com.govos.ntf.entity.NotificationPriority;
import com.govos.ntf.entity.NotificationQueue;
import com.govos.ntf.exception.NotificationNotFoundException;
import com.govos.ntf.exception.NotificationQueueNotFoundException;
import com.govos.ntf.mapper.NotificationQueueMapper;
import com.govos.ntf.repository.NotificationQueueRepository;
import com.govos.ntf.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationQueueServiceImpl implements NotificationQueueService {

    private final NotificationQueueRepository notificationQueueRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationQueueMapper notificationQueueMapper;

    public NotificationQueueServiceImpl(
            NotificationQueueRepository notificationQueueRepository,
            NotificationRepository notificationRepository,
            NotificationQueueMapper notificationQueueMapper) {
        this.notificationQueueRepository = notificationQueueRepository;
        this.notificationRepository = notificationRepository;
        this.notificationQueueMapper = notificationQueueMapper;
    }

    @Override
    public NotificationQueueDto getById(UUID id) {
        return notificationQueueMapper.toDto(findActiveById(id));
    }

    @Override
    public List<NotificationQueueDto> getByNotificationId(UUID notificationId) {
        return notificationQueueRepository.findByNotification_IdAndDeletedFalse(notificationId).stream()
                .map(notificationQueueMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationQueueDto create(CreateNotificationQueueRequest request) {
        Notification notification = notificationRepository.findByIdAndDeletedFalse(request.notificationId())
                .orElseThrow(() -> new NotificationNotFoundException(request.notificationId()));

        NotificationQueue entity = new NotificationQueue();
        entity.setCode(request.code());
        entity.setNotification(notification);
        entity.setPriority(request.priority() != null ? request.priority() : NotificationPriority.NORMAL);
        entity.setRetryCount(request.retryCount() != null ? request.retryCount() : 0);
        entity.setMaxRetry(request.maxRetry() != null ? request.maxRetry() : 3);
        entity.setNextRetryAt(request.nextRetryAt());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationQueueMapper.toDto(notificationQueueRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationQueueDto update(UUID id, UpdateNotificationQueueRequest request) {
        NotificationQueue entity = findActiveById(id);
        assertVersion(entity, request.version());

        entity.setCode(request.code());
        if (request.priority() != null) {
            entity.setPriority(request.priority());
        }
        if (request.retryCount() != null) {
            entity.setRetryCount(request.retryCount());
        }
        if (request.maxRetry() != null) {
            entity.setMaxRetry(request.maxRetry());
        }
        entity.setNextRetryAt(request.nextRetryAt());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationQueueMapper.toDto(notificationQueueRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        NotificationQueue entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        notificationQueueRepository.save(entity);
    }

    private NotificationQueue findActiveById(UUID id) {
        return notificationQueueRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationQueueNotFoundException(id));
    }

    private void assertVersion(NotificationQueue entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "NotificationQueue version mismatch for id: " + entity.getId());
        }
    }
}
