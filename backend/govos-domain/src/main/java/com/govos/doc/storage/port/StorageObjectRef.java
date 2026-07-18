package com.govos.doc.storage.port;

public record StorageObjectRef(
        String bucket,
        String key,
        String versionId) {

    public StorageObjectRef(String bucket, String key) {
        this(bucket, key, null);
    }
}
