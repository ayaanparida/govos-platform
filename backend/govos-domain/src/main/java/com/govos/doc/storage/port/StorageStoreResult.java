package com.govos.doc.storage.port;

public record StorageStoreResult(
        StorageObjectRef objectRef,
        StorageObjectMetadata metadata,
        long bytesStored) {
}
