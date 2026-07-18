package com.govos.doc.storage.support;

import com.govos.doc.storage.port.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class StorageStreamSupport {

    private StorageStreamSupport() {
    }

    public static long copy(InputStream inputStream, OutputStream outputStream, int bufferSize) {
        byte[] buffer = new byte[bufferSize];
        long total = 0L;
        try {
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
                total += read;
            }
            outputStream.flush();
            return total;
        } catch (Exception ex) {
            throw new StorageException("Failed to stream storage object", ex);
        }
    }
}
