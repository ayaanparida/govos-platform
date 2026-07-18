package com.govos.doc.service.impl;

import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.event.DocumentEvents;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.exception.ValidationResult;
import com.govos.doc.mapper.StorageProviderMapper;
import com.govos.doc.repository.StorageProviderRepository;
import com.govos.doc.service.StorageProviderService;
import com.govos.doc.validator.StorageProviderValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StorageProviderServiceImpl implements StorageProviderService {

    private final StorageProviderRepository storageProviderRepository;
    private final StorageProviderMapper storageProviderMapper;
    private final StorageProviderValidator storageProviderValidator;
    private final DocumentEventPublisher eventPublisher;

    public StorageProviderServiceImpl(
            StorageProviderRepository storageProviderRepository,
            StorageProviderMapper storageProviderMapper,
            StorageProviderValidator storageProviderValidator,
            DocumentEventPublisher eventPublisher) {
        this.storageProviderRepository = storageProviderRepository;
        this.storageProviderMapper = storageProviderMapper;
        this.storageProviderValidator = storageProviderValidator;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public StorageProvider createProvider(CreateStorageProviderRequest request) {
        storageProviderValidator.validateCreate(request);
        assertUniqueProviderName(request.providerName(), null);

        StorageProvider entity = storageProviderMapper.toEntity(request);
        entity.setDeleted(false);
        entity.setActive(request.active() != null ? request.active() : true);
        if (entity.getIsDefault() == null) {
            entity.setIsDefault(false);
        }
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearExistingDefault(null);
        }

        StorageProvider saved = storageProviderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.storageProviderCreated(saved));
        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            eventPublisher.publish(DocumentEvents.storageProviderDefaultChanged(saved));
        }
        return saved;
    }

    @Override
    @Transactional
    public StorageProvider updateProvider(UUID id, UpdateStorageProviderRequest request) {
        StorageProvider entity = findActiveById(id);
        assertVersion(entity, request.version());
        storageProviderValidator.validateUpdate(request);
        boolean wasDefault = Boolean.TRUE.equals(entity.getIsDefault());

        if (request.providerName() != null) {
            assertUniqueProviderName(request.providerName(), id);
        }

        storageProviderMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        if (Boolean.TRUE.equals(request.isDefault())) {
            clearExistingDefault(id);
            entity.setIsDefault(true);
        } else if (Boolean.FALSE.equals(request.isDefault())) {
            entity.setIsDefault(false);
        }

        StorageProvider saved = storageProviderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.storageProviderUpdated(saved));
        if (Boolean.TRUE.equals(saved.getIsDefault()) && !wasDefault) {
            eventPublisher.publish(DocumentEvents.storageProviderDefaultChanged(saved));
        }
        return saved;
    }

    @Override
    @Transactional
    public StorageProvider activateProvider(UUID id) {
        StorageProvider entity = findActiveById(id);
        entity.setActive(true);
        StorageProvider saved = storageProviderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.storageProviderActivated(saved));
        return saved;
    }

    @Override
    @Transactional
    public StorageProvider deactivateProvider(UUID id) {
        StorageProvider entity = findActiveById(id);
        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            throw new DocumentValidationException("Cannot deactivate the default storage provider");
        }
        entity.setActive(false);
        StorageProvider saved = storageProviderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.storageProviderDeactivated(saved));
        return saved;
    }

    @Override
    @Transactional
    public StorageProvider setDefaultProvider(UUID id) {
        StorageProvider entity = findActiveById(id);
        if (!Boolean.TRUE.equals(entity.getActive())) {
            throw new DocumentValidationException("Default storage provider must be active");
        }
        clearExistingDefault(id);
        entity.setIsDefault(true);
        StorageProvider saved = storageProviderRepository.save(entity);
        eventPublisher.publish(DocumentEvents.storageProviderDefaultChanged(saved));
        return saved;
    }

    @Override
    public StorageProvider findProvider(UUID id) {
        return findActiveById(id);
    }

    private StorageProvider findActiveById(UUID id) {
        return storageProviderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new StorageProviderNotFoundException(id));
    }

    private void assertUniqueProviderName(String providerName, UUID excludeId) {
        if (!StringUtils.hasText(providerName)) {
            return;
        }
        storageProviderRepository.findByProviderNameAndDeletedFalse(providerName)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    ValidationResult result = new ValidationResult();
                    result.addError(
                            "providerName",
                            "Storage provider name already exists",
                            "DOC_DUPLICATE_PROVIDER_NAME");
                    result.throwIfInvalid();
                });
    }

    private void clearExistingDefault(UUID excludeId) {
        storageProviderRepository.findByIsDefaultTrueAndDeletedFalse()
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .ifPresent(existing -> {
                    existing.setIsDefault(false);
                    storageProviderRepository.save(existing);
                });
        List<StorageProvider> activeDefaults = storageProviderRepository.findByActiveTrueAndDeletedFalse().stream()
                .filter(StorageProvider::getIsDefault)
                .filter(existing -> excludeId == null || !existing.getId().equals(excludeId))
                .toList();
        for (StorageProvider provider : activeDefaults) {
            provider.setIsDefault(false);
            storageProviderRepository.save(provider);
        }
    }

    private void assertVersion(StorageProvider entity, Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(entity.getVersion())) {
            throw new DocumentValidationException("Optimistic lock conflict for storage provider " + entity.getId());
        }
    }
}
