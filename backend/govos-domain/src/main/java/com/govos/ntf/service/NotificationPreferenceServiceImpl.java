package com.govos.ntf.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.ntf.dto.CreateNotificationPreferenceRequest;
import com.govos.ntf.dto.NotificationPreferenceDto;
import com.govos.ntf.dto.UpdateNotificationPreferenceRequest;
import com.govos.ntf.entity.NotificationChannel;
import com.govos.ntf.entity.NotificationPreference;
import com.govos.ntf.exception.NotificationChannelNotFoundException;
import com.govos.ntf.exception.NotificationPreferenceNotFoundException;
import com.govos.ntf.mapper.NotificationPreferenceMapper;
import com.govos.ntf.repository.NotificationChannelRepository;
import com.govos.ntf.repository.NotificationPreferenceRepository;
import com.govos.ntf.validator.NotificationPreferenceValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final UserRepository userRepository;
    private final NotificationPreferenceMapper notificationPreferenceMapper;
    private final NotificationPreferenceValidator notificationPreferenceValidator;

    public NotificationPreferenceServiceImpl(
            NotificationPreferenceRepository notificationPreferenceRepository,
            NotificationChannelRepository notificationChannelRepository,
            UserRepository userRepository,
            NotificationPreferenceMapper notificationPreferenceMapper,
            NotificationPreferenceValidator notificationPreferenceValidator) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.notificationChannelRepository = notificationChannelRepository;
        this.userRepository = userRepository;
        this.notificationPreferenceMapper = notificationPreferenceMapper;
        this.notificationPreferenceValidator = notificationPreferenceValidator;
    }

    @Override
    public NotificationPreferenceDto getById(UUID id) {
        return notificationPreferenceMapper.toDto(findActiveById(id));
    }

    @Override
    public List<NotificationPreferenceDto> getByUserId(UUID userId) {
        return notificationPreferenceRepository.findByUser_IdAndDeletedFalse(userId).stream()
                .map(notificationPreferenceMapper::toDto)
                .toList();
    }

    @Override
    public NotificationPreferenceDto getByUserIdAndChannelId(UUID userId, UUID channelId) {
        return notificationPreferenceMapper.toDto(
                notificationPreferenceRepository.findByUser_IdAndChannel_IdAndDeletedFalse(userId, channelId)
                        .orElseThrow(() -> new NotificationPreferenceNotFoundException(userId)));
    }

    @Override
    @Transactional
    public NotificationPreferenceDto create(CreateNotificationPreferenceRequest request) {
        notificationPreferenceValidator.validateCreate(request);

        NotificationPreference entity = new NotificationPreference();
        entity.setCode(request.code());
        entity.setUser(resolveUser(request.userId()));
        entity.setChannel(resolveChannel(request.channelId()));
        entity.setEnabled(request.enabled() != null ? request.enabled() : true);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationPreferenceMapper.toDto(notificationPreferenceRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationPreferenceDto update(UUID id, UpdateNotificationPreferenceRequest request) {
        NotificationPreference entity = findActiveById(id);
        assertVersion(entity, request.version());

        entity.setCode(request.code());
        entity.setEnabled(request.enabled());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationPreferenceMapper.toDto(notificationPreferenceRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        NotificationPreference entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        notificationPreferenceRepository.save(entity);
    }

    private User resolveUser(UUID userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private NotificationChannel resolveChannel(UUID channelId) {
        return notificationChannelRepository.findByIdAndDeletedFalse(channelId)
                .orElseThrow(() -> new NotificationChannelNotFoundException(channelId));
    }

    private NotificationPreference findActiveById(UUID id) {
        return notificationPreferenceRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationPreferenceNotFoundException(id));
    }

    private void assertVersion(NotificationPreference entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "NotificationPreference version mismatch for id: " + entity.getId());
        }
    }
}
