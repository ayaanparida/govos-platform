package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationDeliveryRequest;
import com.govos.ntf.dto.NotificationDeliveryDto;
import com.govos.ntf.dto.UpdateNotificationDeliveryRequest;
import com.govos.ntf.entity.DeliveryStatus;
import com.govos.ntf.entity.Notification;
import com.govos.ntf.entity.NotificationDelivery;
import com.govos.ntf.exception.NotificationDeliveryNotFoundException;
import com.govos.ntf.exception.NotificationNotFoundException;
import com.govos.ntf.mapper.NotificationDeliveryMapper;
import com.govos.ntf.repository.NotificationDeliveryRepository;
import com.govos.ntf.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryMapper notificationDeliveryMapper;

    public NotificationDeliveryServiceImpl(
            NotificationDeliveryRepository notificationDeliveryRepository,
            NotificationRepository notificationRepository,
            NotificationDeliveryMapper notificationDeliveryMapper) {
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryMapper = notificationDeliveryMapper;
    }

    @Override
    public NotificationDeliveryDto getById(UUID id) {
        return notificationDeliveryMapper.toDto(findActiveById(id));
    }

    @Override
    public List<NotificationDeliveryDto> getByNotificationId(UUID notificationId) {
        return notificationDeliveryRepository.findByNotification_IdAndDeletedFalseOrderByCreatedDateDesc(notificationId)
                .stream()
                .map(notificationDeliveryMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationDeliveryDto> getByDeliveryStatus(DeliveryStatus deliveryStatus) {
        return notificationDeliveryRepository.findByDeliveryStatusAndDeletedFalseOrderByCreatedDateDesc(deliveryStatus)
                .stream()
                .map(notificationDeliveryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationDeliveryDto create(CreateNotificationDeliveryRequest request) {
        Notification notification = notificationRepository.findByIdAndDeletedFalse(request.notificationId())
                .orElseThrow(() -> new NotificationNotFoundException(request.notificationId()));

        NotificationDelivery entity = new NotificationDelivery();
        entity.setCode(request.code());
        entity.setNotification(notification);
        entity.setDeliveryStatus(request.deliveryStatus() != null
                ? request.deliveryStatus() : DeliveryStatus.PENDING);
        entity.setProviderReference(request.providerReference());
        entity.setRetryCount(request.retryCount() != null ? request.retryCount() : 0);
        entity.setMaxRetry(request.maxRetry() != null ? request.maxRetry() : 3);
        entity.setNextRetryAt(request.nextRetryAt());
        entity.setLastAttempt(request.lastAttempt());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationDeliveryMapper.toDto(notificationDeliveryRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationDeliveryDto update(UUID id, UpdateNotificationDeliveryRequest request) {
        NotificationDelivery entity = findActiveById(id);
        assertVersion(entity, request.version());

        entity.setCode(request.code());
        if (request.deliveryStatus() != null) {
            entity.setDeliveryStatus(request.deliveryStatus());
        }
        entity.setProviderReference(request.providerReference());
        if (request.retryCount() != null) {
            entity.setRetryCount(request.retryCount());
        }
        if (request.maxRetry() != null) {
            entity.setMaxRetry(request.maxRetry());
        }
        entity.setNextRetryAt(request.nextRetryAt());
        entity.setLastAttempt(request.lastAttempt());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationDeliveryMapper.toDto(notificationDeliveryRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        NotificationDelivery entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        notificationDeliveryRepository.save(entity);
    }

    private NotificationDelivery findActiveById(UUID id) {
        return notificationDeliveryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationDeliveryNotFoundException(id));
    }

    private void assertVersion(NotificationDelivery entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "NotificationDelivery version mismatch for id: " + entity.getId());
        }
    }
}
