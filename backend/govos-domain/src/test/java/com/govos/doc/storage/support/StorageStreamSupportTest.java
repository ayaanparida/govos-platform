package com.govos.doc.storage.support;

import com.govos.doc.storage.port.StorageException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageStreamSupportTest {

    @Test
    void shouldCopyStreamWithBuffer() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        long copied = StorageStreamSupport.copy(
                new ByteArrayInputStream("stream-data".getBytes()),
                outputStream,
                4);

        assertThat(copied).isEqualTo(11L);
        assertThat(outputStream.toString()).isEqualTo("stream-data");
    }

    @Test
    void shouldHashStorageObjectKey() {
        String hash = StorageObjectKeyHasher.hashKey("org/doc/file.txt");

        assertThat(hash).hasSize(16);
    }

    @Test
    void shouldTreatBlankKeyAsEmptyHash() {
        assertThat(StorageObjectKeyHasher.hashKey("")).isEqualTo("empty");
    }

    @Test
    void shouldWrapCopyFailuresAsStorageException() {
        assertThatThrownBy(() -> StorageStreamSupport.copy(
                        new ByteArrayInputStream("x".getBytes()),
                        new java.io.OutputStream() {
                            @Override
                            public void write(int b) {
                                throw new RuntimeException("broken");
                            }
                        },
                        2))
                .isInstanceOf(StorageException.class);
    }
}
