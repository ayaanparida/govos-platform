package com.govos.ntf.service;

import com.govos.ntf.dto.CreateNotificationTemplateRequest;
import com.govos.ntf.dto.NotificationTemplateDto;
import com.govos.ntf.dto.UpdateNotificationTemplateRequest;
import com.govos.ntf.entity.NotificationChannel;
import com.govos.ntf.entity.NotificationTemplate;
import com.govos.ntf.exception.NotificationChannelNotFoundException;
import com.govos.ntf.exception.NotificationTemplateNotFoundException;
import com.govos.ntf.mapper.NotificationTemplateMapper;
import com.govos.ntf.repository.NotificationChannelRepository;
import com.govos.ntf.repository.NotificationTemplateRepository;
import com.govos.ntf.validator.NotificationTemplateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final NotificationTemplateMapper notificationTemplateMapper;
    private final NotificationTemplateValidator notificationTemplateValidator;

    public NotificationTemplateServiceImpl(
            NotificationTemplateRepository notificationTemplateRepository,
            NotificationChannelRepository notificationChannelRepository,
            NotificationTemplateMapper notificationTemplateMapper,
            NotificationTemplateValidator notificationTemplateValidator) {
        this.notificationTemplateRepository = notificationTemplateRepository;
        this.notificationChannelRepository = notificationChannelRepository;
        this.notificationTemplateMapper = notificationTemplateMapper;
        this.notificationTemplateValidator = notificationTemplateValidator;
    }

    @Override
    public NotificationTemplateDto getById(UUID id) {
        return notificationTemplateMapper.toDto(findActiveById(id));
    }

    @Override
    public NotificationTemplateDto getByCode(String code) {
        return notificationTemplateMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<NotificationTemplateDto> getAll() {
        return notificationTemplateRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(notificationTemplateMapper::toDto)
                .toList();
    }

    @Override
    public List<NotificationTemplateDto> getByChannelId(UUID channelId) {
        return notificationTemplateRepository.findByChannel_IdAndDeletedFalseOrderByNameAsc(channelId).stream()
                .map(notificationTemplateMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public NotificationTemplateDto create(CreateNotificationTemplateRequest request) {
        notificationTemplateValidator.validateCreate(request);

        NotificationTemplate entity = new NotificationTemplate();
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setChannel(resolveChannel(request.channelId()));
        entity.setSubjectTemplate(request.subjectTemplate());
        entity.setBodyTemplate(request.bodyTemplate());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return notificationTemplateMapper.toDto(notificationTemplateRepository.save(entity));
    }

    @Override
    @Transactional
    public NotificationTemplateDto update(UUID id, UpdateNotificationTemplateRequest request) {
        NotificationTemplate entity = findActiveById(id);
        assertVersion(entity, request.version());
        notificationTemplateValidator.validateUpdate(id, request);

        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setChannel(resolveChannel(request.channelId()));
        entity.setSubjectTemplate(request.subjectTemplate());
        entity.setBodyTemplate(request.bodyTemplate());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return notificationTemplateMapper.toDto(notificationTemplateRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        NotificationTemplate entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        notificationTemplateRepository.save(entity);
    }

    private NotificationChannel resolveChannel(UUID channelId) {
        return notificationChannelRepository.findByIdAndDeletedFalse(channelId)
                .orElseThrow(() -> new NotificationChannelNotFoundException(channelId));
    }

    private NotificationTemplate findActiveById(UUID id) {
        return notificationTemplateRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(id));
    }

    private NotificationTemplate findActiveByCode(String code) {
        return notificationTemplateRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new NotificationTemplateNotFoundException(code));
    }

    private void assertVersion(NotificationTemplate entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "NotificationTemplate version mismatch for id: " + entity.getId());
        }
    }
}
