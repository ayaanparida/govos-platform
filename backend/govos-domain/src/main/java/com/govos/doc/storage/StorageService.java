package com.govos.doc.storage;

import com.govos.doc.entity.StorageProviderType;

/**
 * Abstraction for external document binary storage.
 * <p>
 * Upload and download implementations are deferred to a later sprint.
 */
public interface StorageService {

    StorageProviderType getProviderType();

    void upload(StorageObjectReference reference, byte[] content);

    byte[] download(StorageObjectReference reference);

    void delete(StorageObjectReference reference);
}
