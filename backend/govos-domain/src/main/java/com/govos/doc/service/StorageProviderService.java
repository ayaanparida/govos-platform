package com.govos.doc.service;

import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.entity.StorageProvider;

import java.util.UUID;

public interface StorageProviderService {

    StorageProvider createProvider(CreateStorageProviderRequest request);

    StorageProvider updateProvider(UUID id, UpdateStorageProviderRequest request);

    StorageProvider activateProvider(UUID id);

    StorageProvider deactivateProvider(UUID id);

    StorageProvider setDefaultProvider(UUID id);

    StorageProvider findProvider(UUID id);
}
