package com.govos.doc.service;

import com.govos.doc.dto.CreateStorageProviderRequest;
import com.govos.doc.dto.StorageProviderDto;
import com.govos.doc.dto.UpdateStorageProviderRequest;
import com.govos.doc.entity.StorageProviderType;

import java.util.List;
import java.util.UUID;

public interface StorageProviderService {

    StorageProviderDto getById(UUID id);

    StorageProviderDto getByCode(String code);

    List<StorageProviderDto> getAll();

    List<StorageProviderDto> getByProviderType(StorageProviderType providerType);

    StorageProviderDto create(CreateStorageProviderRequest request);

    StorageProviderDto update(UUID id, UpdateStorageProviderRequest request);

    void softDelete(UUID id);
}
