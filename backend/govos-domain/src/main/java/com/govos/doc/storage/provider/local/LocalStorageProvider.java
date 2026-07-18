package com.govos.doc.storage.provider.local;

import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.metrics.StorageMetricsRecorder;
import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageException;
import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageProviderPort;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import com.govos.doc.storage.support.StorageStreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Component
public class LocalStorageProvider implements StorageProviderPort {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageProvider.class);

    private final DocumentStorageProperties properties;
    private final StorageMetricsRecorder metricsRecorder;
    private final Map<String, Path> multipartSessions = new ConcurrentHashMap<>();

    public LocalStorageProvider(DocumentStorageProperties properties, StorageMetricsRecorder metricsRecorder) {
        this.properties = properties;
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public StorageStoreResult store(StorageStoreRequest request, InputStream inputStream) {
        long start = System.nanoTime();
        try {
            Path target = resolveObjectPath(request.bucket(), request.key());
            Files.createDirectories(target.getParent());
            long bytes;
            try (OutputStream outputStream = new BufferedOutputStream(
                    Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
                bytes = StorageStreamSupport.copy(inputStream, outputStream, properties.getBufferSize());
            }
            StorageObjectMetadata metadata = buildMetadata(target, request.contentType(), request.checksum());
            metricsRecorder.recordUpload(providerName(), bytes);
            log.info(
                    "storage provider={} operation=store documentId={} keyHash={} durationMs={} status=success bytes={}",
                    providerName(),
                    request.documentId(),
                    com.govos.doc.storage.support.StorageObjectKeyHasher.hashKey(request.key()),
                    elapsedMs(start),
                    bytes);
            return new StorageStoreResult(new StorageObjectRef(request.bucket(), request.key()), metadata, bytes);
        } catch (RuntimeException | IOException ex) {
            metricsRecorder.recordFailure(providerName());
            log.warn(
                    "storage provider={} operation=store documentId={} durationMs={} status=failure",
                    providerName(),
                    request.documentId(),
                    elapsedMs(start));
            throw ex instanceof StorageException ? (StorageException) ex
                    : new StorageException("Failed to store object locally", ex);
        }
    }

    @Override
    public InputStream load(StorageObjectRef objectRef) {
        long start = System.nanoTime();
        try {
            Path path = resolveObjectPath(objectRef.bucket(), objectRef.key());
            if (!Files.exists(path)) {
                throw new StorageException("Object not found: " + objectRef.key());
            }
            long size = Files.size(path);
            metricsRecorder.recordDownload(providerName(), size);
            log.info(
                    "storage provider={} operation=load keyHash={} durationMs={} status=success bytes={}",
                    providerName(),
                    com.govos.doc.storage.support.StorageObjectKeyHasher.hashKey(objectRef.key()),
                    elapsedMs(start),
                    size);
            return new BufferedInputStream(Files.newInputStream(path));
        } catch (IOException ex) {
            metricsRecorder.recordFailure(providerName());
            throw new StorageException("Failed to load object locally", ex);
        }
    }

    @Override
    public void delete(StorageObjectRef objectRef) {
        try {
            Files.deleteIfExists(resolveObjectPath(objectRef.bucket(), objectRef.key()));
            metricsRecorder.recordDelete(providerName());
            log.info(
                    "storage provider={} operation=delete keyHash={} status=success",
                    providerName(),
                    com.govos.doc.storage.support.StorageObjectKeyHasher.hashKey(objectRef.key()));
        } catch (IOException ex) {
            metricsRecorder.recordFailure(providerName());
            throw new StorageException("Failed to delete object locally", ex);
        }
    }

    @Override
    public boolean exists(StorageObjectRef objectRef) {
        return Files.exists(resolveObjectPath(objectRef.bucket(), objectRef.key()));
    }

    @Override
    public void copy(StorageObjectRef source, StorageObjectRef destination) {
        try {
            Files.createDirectories(resolveObjectPath(destination.bucket(), destination.key()).getParent());
            Files.copy(
                    resolveObjectPath(source.bucket(), source.key()),
                    resolveObjectPath(destination.bucket(), destination.key()),
                    StandardCopyOption.REPLACE_EXISTING);
            metricsRecorder.recordCopy(providerName());
        } catch (IOException ex) {
            metricsRecorder.recordFailure(providerName());
            throw new StorageException("Failed to copy object locally", ex);
        }
    }

    @Override
    public void move(StorageObjectRef source, StorageObjectRef destination) {
        try {
            Files.createDirectories(resolveObjectPath(destination.bucket(), destination.key()).getParent());
            Files.move(
                    resolveObjectPath(source.bucket(), source.key()),
                    resolveObjectPath(destination.bucket(), destination.key()),
                    StandardCopyOption.REPLACE_EXISTING);
            metricsRecorder.recordMove(providerName());
        } catch (IOException ex) {
            metricsRecorder.recordFailure(providerName());
            throw new StorageException("Failed to move object locally", ex);
        }
    }

    @Override
    public SignedUrlResult generateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration) {
        String url = "local://download/" + objectRef.bucket() + "/" + objectRef.key();
        return new SignedUrlResult(url, Instant.now().plus(expiration), "GET");
    }

    @Override
    public SignedUrlResult generateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration) {
        String url = "local://upload/" + objectRef.bucket() + "/" + objectRef.key();
        return new SignedUrlResult(url, Instant.now().plus(expiration), "PUT");
    }

    @Override
    public StorageObjectMetadata getMetadata(StorageObjectRef objectRef) {
        try {
            Path path = resolveObjectPath(objectRef.bucket(), objectRef.key());
            if (!Files.exists(path)) {
                throw new StorageException("Object not found: " + objectRef.key());
            }
            return buildMetadata(path, null, null);
        } catch (IOException ex) {
            throw new StorageException("Failed to read metadata locally", ex);
        }
    }

    @Override
    public List<StorageObjectRef> listObjects(String prefix, int maxKeys) {
        Path root = resolveBucketPath(properties.getBucket());
        if (!Files.exists(root)) {
            return List.of();
        }
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(Files::isRegularFile)
                    .map(root::relativize)
                    .map(Path::toString)
                    .filter(key -> prefix == null || prefix.isBlank() || key.startsWith(prefix))
                    .sorted()
                    .limit(Math.max(maxKeys, 1))
                    .map(key -> new StorageObjectRef(properties.getBucket(), key.replace('\\', '/')))
                    .toList();
        } catch (IOException ex) {
            throw new StorageException("Failed to list local objects", ex);
        }
    }

    @Override
    public StorageHealth health() {
        try {
            Path base = Path.of(properties.getLocal().getBasePath());
            Files.createDirectories(base);
            Path probe = base.resolve(".health-" + UUID.randomUUID());
            Files.writeString(probe, "ok");
            Files.delete(probe);
            return StorageHealth.up();
        } catch (IOException ex) {
            return StorageHealth.down("Local storage path is not writable");
        }
    }

    @Override
    public String providerName() {
        return "local";
    }

    @Override
    public StorageProviderType providerType() {
        return StorageProviderType.LOCAL;
    }

    @Override
    public MultipartUploadSession beginMultipartUpload(MultipartUploadRequest request) {
        String uploadId = UUID.randomUUID().toString();
        Path sessionDir = Path.of(properties.getLocal().getBasePath(), ".multipart", uploadId);
        try {
            Files.createDirectories(sessionDir);
        } catch (IOException ex) {
            throw new StorageException("Failed to begin multipart upload", ex);
        }
        multipartSessions.put(uploadId, sessionDir);
        return new MultipartUploadSession(
                uploadId,
                new StorageObjectRef(request.bucket(), request.key()),
                request.contentType());
    }

    @Override
    public MultipartPartResult uploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize) {
        Path sessionDir = requireSessionDir(session.uploadId());
        Path partPath = sessionDir.resolve("part-" + partNumber);
        try (OutputStream outputStream = Files.newOutputStream(partPath)) {
            long written = StorageStreamSupport.copy(partStream, outputStream, properties.getBufferSize());
            return new MultipartPartResult(partNumber, "local-part-" + partNumber, written);
        } catch (IOException ex) {
            throw new StorageException("Failed to upload multipart part", ex);
        }
    }

    @Override
    public StorageStoreResult completeMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts) {
        Path sessionDir = requireSessionDir(session.uploadId());
        Path target = resolveObjectPath(session.objectRef().bucket(), session.objectRef().key());
        try {
            Files.createDirectories(target.getParent());
            List<MultipartPartResult> orderedParts = new ArrayList<>(parts);
            orderedParts.sort(Comparator.comparingInt(MultipartPartResult::partNumber));
            long totalBytes = 0L;
            try (OutputStream outputStream = Files.newOutputStream(
                    target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (MultipartPartResult part : orderedParts) {
                    Path partPath = sessionDir.resolve("part-" + part.partNumber());
                    totalBytes += StorageStreamSupport.copy(
                            Files.newInputStream(partPath), outputStream, properties.getBufferSize());
                }
            }
            cleanupSession(session.uploadId());
            StorageObjectMetadata metadata = buildMetadata(target, session.contentType(), null);
            metricsRecorder.recordUpload(providerName(), totalBytes);
            return new StorageStoreResult(session.objectRef(), metadata, totalBytes);
        } catch (IOException ex) {
            metricsRecorder.recordFailure(providerName());
            throw new StorageException("Failed to complete multipart upload", ex);
        }
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        cleanupSession(session.uploadId());
    }

    private Path requireSessionDir(String uploadId) {
        Path sessionDir = multipartSessions.get(uploadId);
        if (sessionDir == null) {
            throw new StorageException("Unknown multipart upload session: " + uploadId);
        }
        return sessionDir;
    }

    private void cleanupSession(String uploadId) {
        Path sessionDir = multipartSessions.remove(uploadId);
        if (sessionDir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(sessionDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // best effort cleanup
                }
            });
        } catch (IOException ex) {
            throw new StorageException("Failed to cleanup multipart session", ex);
        }
    }

    private Path resolveObjectPath(String bucket, String key) {
        return resolveBucketPath(bucket).resolve(key.replace('/', java.io.File.separatorChar));
    }

    private Path resolveBucketPath(String bucket) {
        return Path.of(properties.getLocal().getBasePath(), bucket);
    }

    private StorageObjectMetadata buildMetadata(Path path, String contentType, String checksum) throws IOException {
        Instant modified = Files.getLastModifiedTime(path).toInstant();
        return new StorageObjectMetadata(
                contentType,
                Files.size(path),
                checksum,
                modified,
                modified,
                "STANDARD",
                String.valueOf(Files.size(path)));
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
