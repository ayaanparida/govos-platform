package com.govos.doc.storage.support;

import com.govos.doc.storage.port.StorageException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class StorageObjectKeyHasher {

    private StorageObjectKeyHasher() {
    }

    public static String hashKey(String storageObjectKey) {
        if (storageObjectKey == null || storageObjectKey.isBlank()) {
            return "empty";
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(storageObjectKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed).substring(0, 16);
        } catch (NoSuchAlgorithmException ex) {
            throw new StorageException("Unable to hash storage object key", ex);
        }
    }
}
