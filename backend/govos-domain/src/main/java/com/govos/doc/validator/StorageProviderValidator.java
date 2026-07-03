package com.govos.doc.validator;

import com.govos.doc.dto.CreateStorageProviderRequest;
import com.govos.doc.dto.UpdateStorageProviderRequest;
import com.govos.doc.exception.DuplicateCodeException;
import com.govos.doc.repository.StorageProviderRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StorageProviderValidator {

    private final StorageProviderRepository storageProviderRepository;

    public StorageProviderValidator(StorageProviderRepository storageProviderRepository) {
        this.storageProviderRepository = storageProviderRepository;
    }

    public void validateCreate(CreateStorageProviderRequest request) {
        if (storageProviderRepository.existsByCodeAndDeletedFalse(request.code())) {
            throw new DuplicateCodeException("StorageProvider", request.code());
        }
    }

    public void validateUpdate(UUID id, UpdateStorageProviderRequest request) {
        storageProviderRepository.findByCodeAndDeletedFalse(request.code())
                .filter(entity -> !entity.getId().equals(id))
                .ifPresent(entity -> {
                    throw new DuplicateCodeException("StorageProvider", request.code());
                });
    }
}
