package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationChannelRequest;
import com.govos.ntf.dto.NotificationChannelDto;
import com.govos.ntf.dto.UpdateNotificationChannelRequest;
import com.govos.ntf.entity.ChannelProvider;
import com.govos.ntf.entity.NotificationChannel;
import com.govos.ntf.exception.NotificationChannelNotFoundException;
import com.govos.ntf.mapper.NotificationChannelMapper;
import com.govos.ntf.repository.NotificationChannelRepository;
import com.govos.ntf.validator.NotificationChannelValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationChannelServiceImpl implements NotificationChannelService {

    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationChannelMapper notificationChannelMapper;
    private final NotificationChannelValidator notificationChannelValidator;

    public NotificationChannelServiceImpl(
            NotificationChannelRepository notificationChannelRepository,
            NotificationChannelMapper notificationChannelMapper,
            NotificationChannelValidator notificationChannelValidator) {
        this.notificationChannelRepository = notificationChannelRepository;
        this.notificationChannelMapper = notificationChannelMapper;
        this.notificationChannelValidator = notificationChannelValidator;
    }

    @Override
    public NotificationChannelDto getById(UUID id) {
        return notificationChannelMapper.toDto(findActiveById(id));
    }

    @Override
    public NotificationChannelDto getByCode(String code) {
        return notificationChannelMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<NotificationChannelDto> getAll() {
        return notificationChannelRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(notificationChannelMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationChannelDto> getByProvider(ChannelProvider provider) {
        return notificationChannelRepository.findByProviderAndDeletedFalseOrderByNameAsc(provider).stream()
                .map(notificationChannelMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationChannelDto create(CreateNotificationChannelRequest request) {
        notificationChannelValidator.validateCreate(request);

        NotificationChannel entity = notificationChannelMapper.toEntity(request);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationChannelMapper.toDto(notificationChannelRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationChannelDto update(UUID id, UpdateNotificationChannelRequest request) {
        NotificationChannel entity = findActiveById(id);
        assertVersion(entity, request.version());
        notificationChannelValidator.validateUpdate(id, request);

        notificationChannelMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationChannelMapper.toDto(notificationChannelRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        NotificationChannel entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        notificationChannelRepository.save(entity);
    }

    private NotificationChannel findActiveById(UUID id) {
        return notificationChannelRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationChannelNotFoundException(id));
    }

    private NotificationChannel findActiveByCode(String code) {
        return notificationChannelRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new NotificationChannelNotFoundException(code));
    }

    private void assertVersion(NotificationChannel entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "NotificationChannel version mismatch for id: " + entity.getId());
        }
    }
}
