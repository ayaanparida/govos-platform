package com.govos.doc.storage;

/**
 * Reference to a stored object in an external storage provider.
 */
public record StorageObjectReference(
        String bucketName,
        String storedFilename,
        String checksum
) {
}
