package com.govos.doc.service;

import com.govos.doc.dto.CreateStorageProviderRequest;
import com.govos.doc.dto.StorageProviderDto;
import com.govos.doc.dto.UpdateStorageProviderRequest;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.entity.StorageProviderType;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.mapper.StorageProviderMapper;
import com.govos.doc.repository.StorageProviderRepository;
import com.govos.doc.validator.StorageProviderValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StorageProviderServiceImpl implements StorageProviderService {

    private final StorageProviderRepository storageProviderRepository;
    private final StorageProviderMapper storageProviderMapper;
    private final StorageProviderValidator storageProviderValidator;

    public StorageProviderServiceImpl(
            StorageProviderRepository storageProviderRepository,
            StorageProviderMapper storageProviderMapper,
            StorageProviderValidator storageProviderValidator) {
        this.storageProviderRepository = storageProviderRepository;
        this.storageProviderMapper = storageProviderMapper;
        this.storageProviderValidator = storageProviderValidator;
    }

    @Override
    public StorageProviderDto getById(UUID id) {
        return storageProviderMapper.toDto(findActiveById(id));
    }

    @Override
    public StorageProviderDto getByCode(String code) {
        return storageProviderMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<StorageProviderDto> getAll() {
        return storageProviderRepository.findByDeletedFalseOrderByCodeAsc().stream()
                .map(storageProviderMapper::toDto)
                .toList();
    }

    @Override
    public List<StorageProviderDto> getByProviderType(StorageProviderType providerType) {
        return storageProviderRepository.findByProviderTypeAndDeletedFalseOrderByCodeAsc(providerType).stream()
                .map(storageProviderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public StorageProviderDto create(CreateStorageProviderRequest request) {
        storageProviderValidator.validateCreate(request);

        StorageProvider entity = storageProviderMapper.toEntity(request);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return storageProviderMapper.toDto(storageProviderRepository.save(entity));
    }

    @Override
    @Transactional
    public StorageProviderDto update(UUID id, UpdateStorageProviderRequest request) {
        StorageProvider entity = findActiveById(id);
        assertVersion(entity, request.version());
        storageProviderValidator.validateUpdate(id, request);

        storageProviderMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return storageProviderMapper.toDto(storageProviderRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        StorageProvider entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        storageProviderRepository.save(entity);
    }

    private StorageProvider findActiveById(UUID id) {
        return storageProviderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new StorageProviderNotFoundException(id));
    }

    private StorageProvider findActiveByCode(String code) {
        return storageProviderRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new StorageProviderNotFoundException(code));
    }

    private void assertVersion(StorageProvider entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "StorageProvider version mismatch for id: " + entity.getId());
        }
    }
}
