package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationRequest;
import com.govos.ntf.dto.NotificationDto;
import com.govos.ntf.dto.UpdateNotificationRequest;
import com.govos.ntf.entity.Notification;
import com.govos.ntf.entity.NotificationChannel;
import com.govos.ntf.entity.NotificationPriority;
import com.govos.ntf.entity.NotificationStatus;
import com.govos.ntf.exception.NotificationChannelNotFoundException;
import com.govos.ntf.exception.NotificationNotFoundException;
import com.govos.ntf.mapper.NotificationMapper;
import com.govos.ntf.repository.NotificationChannelRepository;
import com.govos.ntf.repository.NotificationRepository;
import com.govos.ntf.validator.NotificationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationValidator notificationValidator;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationChannelRepository notificationChannelRepository,
            NotificationMapper notificationMapper,
            NotificationValidator notificationValidator) {
        this.notificationRepository = notificationRepository;
        this.notificationChannelRepository = notificationChannelRepository;
        this.notificationMapper = notificationMapper;
        this.notificationValidator = notificationValidator;
    }

    @Override
    public NotificationDto getById(UUID id) {
        return notificationMapper.toDto(findActiveById(id));
    }

    @Override
    public NotificationDto getByCode(String code) {
        return notificationMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<NotificationDto> getAll() {
        return notificationRepository.findByDeletedFalseOrderByCreatedDateDesc().stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationDto> getByChannelId(UUID channelId) {
        return notificationRepository.findByChannel_IdAndDeletedFalseOrderByCreatedDateDesc(channelId).stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationDto> getByStatus(NotificationStatus status) {
        return notificationRepository.findByStatusAndDeletedFalseOrderByCreatedDateDesc(status).stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationDto> getByRecipient(String recipient) {
        return notificationRepository.findByRecipientAndDeletedFalseOrderByCreatedDateDesc(recipient).stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationDto create(CreateNotificationRequest request) {
        notificationValidator.validateCreate(request);

        Notification entity = new Notification();
        applyFields(entity, request.code(), request.recipient(), request.subject(), request.body(),
                request.channelId(), request.status(), request.priority(),
                request.scheduledAt(), request.sentAt());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationMapper.toDto(notificationRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationDto update(UUID id, UpdateNotificationRequest request) {
        Notification entity = findActiveById(id);
        assertVersion(entity, request.version());
        notificationValidator.validateUpdate(id, request);

        applyFields(entity, request.code(), request.recipient(), request.subject(), request.body(),
                request.channelId(), request.status(), request.priority(),
                request.scheduledAt(), request.sentAt());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationMapper.toDto(notificationRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Notification entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        entity.setStatus(NotificationStatus.CANCELLED);
        notificationRepository.save(entity);
    }

    private void applyFields(
            Notification entity,
            String code,
            String recipient,
            String subject,
            String body,
            UUID channelId,
            NotificationStatus status,
            NotificationPriority priority,
            java.time.Instant scheduledAt,
            java.time.Instant sentAt) {
        entity.setCode(code);
        entity.setRecipient(recipient);
        entity.setSubject(subject);
        entity.setBody(body);
        entity.setChannel(resolveChannel(channelId));
        entity.setStatus(status != null ? status : NotificationStatus.PENDING);
        entity.setPriority(priority != null ? priority : NotificationPriority.NORMAL);
        entity.setScheduledAt(scheduledAt);
        entity.setSentAt(sentAt);
    }

    private NotificationChannel resolveChannel(UUID channelId) {
        return notificationChannelRepository.findByIdAndDeletedFalse(channelId)
                .orElseThrow(() -> new NotificationChannelNotFoundException(channelId));
    }

    private Notification findActiveById(UUID id) {
        return notificationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }

    private Notification findActiveByCode(String code) {
        return notificationRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new NotificationNotFoundException(code));
    }

    private void assertVersion(Notification entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Notification version mismatch for id: " + entity.getId());
        }
    }
}
