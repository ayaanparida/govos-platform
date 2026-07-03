package com.govos.doc.storage;

import com.govos.doc.entity.StorageProviderType;
import org.springframework.stereotype.Service;

/**
 * MinIO storage provider adapter.
 * <p>
 * SDK integration and upload/download logic are not implemented in Sprint 0 Day 6.
 */
@Service
public class MinioStorageService implements StorageService {

    private static final String NOT_IMPLEMENTED =
            "MinIO storage operations are not implemented in Sprint 0 Day 6";

    @Override
    public StorageProviderType getProviderType() {
        return StorageProviderType.MINIO;
    }

    @Override
    public void upload(StorageObjectReference reference, byte[] content) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public byte[] download(StorageObjectReference reference) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    @Override
    public void delete(StorageObjectReference reference) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
