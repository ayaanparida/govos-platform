package com.govos.ntf.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.ntf.dto.CreateNotificationSubscriptionRequest;
import com.govos.ntf.dto.NotificationSubscriptionDto;
import com.govos.ntf.dto.UpdateNotificationSubscriptionRequest;
import com.govos.ntf.entity.NotificationChannel;
import com.govos.ntf.entity.NotificationSubscription;
import com.govos.ntf.exception.NotificationChannelNotFoundException;
import com.govos.ntf.exception.NotificationSubscriptionNotFoundException;
import com.govos.ntf.mapper.NotificationSubscriptionMapper;
import com.govos.ntf.repository.NotificationChannelRepository;
import com.govos.ntf.repository.NotificationSubscriptionRepository;
import com.govos.ntf.validator.NotificationSubscriptionValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationSubscriptionServiceImpl implements NotificationSubscriptionService {

    private final NotificationSubscriptionRepository notificationSubscriptionRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final UserRepository userRepository;
    private final NotificationSubscriptionMapper notificationSubscriptionMapper;
    private final NotificationSubscriptionValidator notificationSubscriptionValidator;

    public NotificationSubscriptionServiceImpl(
            NotificationSubscriptionRepository notificationSubscriptionRepository,
            NotificationChannelRepository notificationChannelRepository,
            UserRepository userRepository,
            NotificationSubscriptionMapper notificationSubscriptionMapper,
            NotificationSubscriptionValidator notificationSubscriptionValidator) {
        this.notificationSubscriptionRepository = notificationSubscriptionRepository;
        this.notificationChannelRepository = notificationChannelRepository;
        this.userRepository = userRepository;
        this.notificationSubscriptionMapper = notificationSubscriptionMapper;
        this.notificationSubscriptionValidator = notificationSubscriptionValidator;
    }

    @Override
    public NotificationSubscriptionDto getById(UUID id) {
        return notificationSubscriptionMapper.toDto(findActiveById(id));
    }

    @Override
    public List<NotificationSubscriptionDto> getByUserId(UUID userId) {
        return notificationSubscriptionRepository.findByUser_IdAndDeletedFalse(userId).stream()
                .map(notificationSubscriptionMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationSubscriptionDto> getByEventType(String eventType) {
        return notificationSubscriptionRepository.findByEventTypeAndDeletedFalse(eventType).stream()
                .map(notificationSubscriptionMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationSubscriptionDto create(CreateNotificationSubscriptionRequest request) {
        notificationSubscriptionValidator.validateCreate(request);

        NotificationSubscription entity = new NotificationSubscription();
        entity.setCode(request.code());
        entity.setUser(resolveUser(request.userId()));
        entity.setEventType(request.eventType());
        entity.setChannel(resolveChannel(request.channelId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationSubscriptionMapper.toDto(notificationSubscriptionRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationSubscriptionDto update(UUID id, UpdateNotificationSubscriptionRequest request) {
        NotificationSubscription entity = findActiveById(id);
        assertVersion(entity, request.version());
        notificationSubscriptionValidator.validateUpdate(id, entity.getUser().getId(), request);

        entity.setCode(request.code());
        entity.setEventType(request.eventType());
        entity.setChannel(resolveChannel(request.channelId()));
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationSubscriptionMapper.toDto(notificationSubscriptionRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        NotificationSubscription entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        notificationSubscriptionRepository.save(entity);
    }

    private User resolveUser(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private NotificationChannel resolveChannel(UUID channelId) {
        return notificationChannelRepository.findByIdAndDeletedFalse(channelId)
                .orElseThrow(() -> new NotificationChannelNotFoundException(channelId));
    }

    private NotificationSubscription findActiveById(UUID id) {
        return notificationSubscriptionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationSubscriptionNotFoundException(id));
    }

    private void assertVersion(NotificationSubscription entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "NotificationSubscription version mismatch for id: " + entity.getId());
        }
    }
}
