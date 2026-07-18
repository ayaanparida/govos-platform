package com.govos.doc.storage.port;

public record StorageHealth(
        StorageHealthStatus status,
        String message) {

    public static StorageHealth up() {
        return new StorageHealth(StorageHealthStatus.UP, "Storage provider is healthy");
    }

    public static StorageHealth down(String message) {
        return new StorageHealth(StorageHealthStatus.DOWN, message);
    }

    public static StorageHealth degraded(String message) {
        return new StorageHealth(StorageHealthStatus.DEGRADED, message);
    }

    public static StorageHealth unknown(String message) {
        return new StorageHealth(StorageHealthStatus.UNKNOWN, message);
    }
}
