package com.govos.doc.storage.provider.local;

import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.testsupport.StorageTestFixtures;
import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageHealthStatus;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LocalStorageProviderTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @TempDir
    Path tempDir;

    private LocalStorageProvider provider;

    @BeforeEach
    void setUp() {
        DocumentStorageProperties properties = new DocumentStorageProperties();
        properties.getLocal().setBasePath(tempDir.toString());
        properties.setBucket("govos-documents");
        provider = new LocalStorageProvider(properties, StorageTestFixtures.disabledMetrics(properties));
    }

    @Test
    void shouldStoreAndLoadDocument() throws Exception {
        StorageStoreRequest request = new StorageStoreRequest(
                DOCUMENT_ID, "govos-documents", "org/doc/file.txt", "text/plain", 5L, null);

        provider.store(request, new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8)));

        try (InputStream loaded = provider.load(new StorageObjectRef("govos-documents", "org/doc/file.txt"))) {
            assertThat(new String(loaded.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello");
        }
    }

    @Test
    void shouldReportHealthyWhenBasePathWritable() {
        assertThat(provider.health().status()).isEqualTo(StorageHealthStatus.UP);
    }

    @Test
    void shouldGenerateInternalSignedUrls() {
        SignedUrlResult download = provider.generateSignedDownloadUrl(
                new StorageObjectRef("govos-documents", "file.txt"), Duration.ofMinutes(5));

        assertThat(download.url()).startsWith("local://download/");
        assertThat(download.method()).isEqualTo("GET");
    }

    @Test
    void shouldSupportMultipartUploadLifecycle() throws Exception {
        MultipartUploadRequest request = new MultipartUploadRequest(
                DOCUMENT_ID, "govos-documents", "multipart/file.bin", "application/octet-stream");
        MultipartUploadSession session = provider.beginMultipartUpload(request);

        MultipartPartResult part1 = provider.uploadPart(
                session, 1, new ByteArrayInputStream("part-1".getBytes(StandardCharsets.UTF_8)), 6L);
        MultipartPartResult part2 = provider.uploadPart(
                session, 2, new ByteArrayInputStream("part-2".getBytes(StandardCharsets.UTF_8)), 6L);

        StorageStoreResult result = provider.completeMultipartUpload(session, List.of(part1, part2));

        assertThat(result.bytesStored()).isEqualTo(12L);
        assertThat(provider.exists(new StorageObjectRef("govos-documents", "multipart/file.bin"))).isTrue();
    }
}
