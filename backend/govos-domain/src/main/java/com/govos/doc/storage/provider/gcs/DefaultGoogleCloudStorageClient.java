package com.govos.doc.storage.provider.gcs;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageException;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class DefaultGoogleCloudStorageClient implements GoogleCloudStorageClient {

    private final Storage storage;
    private final DocumentStorageProperties properties;
    private final ConcurrentMap<String, MultipartUploadSession> multipartSessions = new ConcurrentHashMap<>();

    public DefaultGoogleCloudStorageClient(Storage storage, DocumentStorageProperties properties) {
        this.storage = storage;
        this.properties = properties;
    }

    public DefaultGoogleCloudStorageClient(DocumentStorageProperties properties) {
        this(buildStorage(properties), properties);
    }

    @Override
    public StorageStoreResult putObject(StorageStoreRequest request, InputStream inputStream) {
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(request.bucket(), request.key())
                    .setContentType(request.contentType())
                    .build();
            Blob blob = storage.createFrom(blobInfo, inputStream);
            StorageObjectMetadata metadata = headObject(new StorageObjectRef(request.bucket(), request.key()));
            return new StorageStoreResult(
                    new StorageObjectRef(request.bucket(), request.key(), blob.getGeneration() != null ? String.valueOf(blob.getGeneration()) : null),
                    metadata,
                    metadata.contentLength());
        } catch (Exception ex) {
            throw new StorageException("GCS putObject failed", ex);
        }
    }

    @Override
    public InputStream getObject(StorageObjectRef objectRef) {
        try {
            Blob blob = storage.get(objectRef.bucket(), objectRef.key());
            if (blob == null) {
                throw new StorageException("GCS object not found: " + objectRef.key());
            }
            return new java.io.ByteArrayInputStream(blob.getContent());
        } catch (StorageException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new StorageException("GCS getObject failed", ex);
        }
    }

    @Override
    public void deleteObject(StorageObjectRef objectRef) {
        storage.delete(BlobId.of(objectRef.bucket(), objectRef.key()));
    }

    @Override
    public boolean objectExists(StorageObjectRef objectRef) {
        Blob blob = storage.get(objectRef.bucket(), objectRef.key());
        return blob != null && blob.exists();
    }

    @Override
    public void copyObject(StorageObjectRef source, StorageObjectRef destination) {
        storage.copy(
                Storage.CopyRequest.newBuilder()
                        .setSource(BlobId.of(source.bucket(), source.key()))
                        .setTarget(BlobId.of(destination.bucket(), destination.key()))
                        .build());
    }

    @Override
    public StorageObjectMetadata headObject(StorageObjectRef objectRef) {
        Blob blob = storage.get(objectRef.bucket(), objectRef.key());
        if (blob == null) {
            throw new StorageException("GCS object not found: " + objectRef.key());
        }
        return new StorageObjectMetadata(
                blob.getContentType(),
                blob.getSize(),
                blob.getEtag(),
                blob.getCreateTimeOffsetDateTime() != null ? blob.getCreateTimeOffsetDateTime().toInstant() : Instant.now(),
                blob.getUpdateTimeOffsetDateTime() != null ? blob.getUpdateTimeOffsetDateTime().toInstant() : Instant.now(),
                blob.getStorageClass() != null ? blob.getStorageClass().name() : "STANDARD",
                blob.getEtag());
    }

    @Override
    public List<StorageObjectRef> listObjects(String bucket, String prefix, int maxKeys) {
        List<StorageObjectRef> refs = new ArrayList<>();
        for (Blob blob : storage.list(bucket, Storage.BlobListOption.prefix(prefix)).iterateAll()) {
            refs.add(new StorageObjectRef(bucket, blob.getName()));
            if (refs.size() >= maxKeys) {
                break;
            }
        }
        return refs;
    }

    @Override
    public SignedUrlResult presignGet(StorageObjectRef objectRef, Duration expiration) {
        URL url = storage.signUrl(
                BlobInfo.newBuilder(objectRef.bucket(), objectRef.key()).build(),
                expiration.toSeconds(),
                TimeUnit.SECONDS,
                Storage.SignUrlOption.httpMethod(com.google.cloud.storage.HttpMethod.GET));
        return new SignedUrlResult(url.toString(), Instant.now().plus(expiration), "GET");
    }

    @Override
    public SignedUrlResult presignPut(StorageObjectRef objectRef, Duration expiration) {
        URL url = storage.signUrl(
                BlobInfo.newBuilder(objectRef.bucket(), objectRef.key()).build(),
                expiration.toSeconds(),
                TimeUnit.SECONDS,
                Storage.SignUrlOption.httpMethod(com.google.cloud.storage.HttpMethod.PUT));
        return new SignedUrlResult(url.toString(), Instant.now().plus(expiration), "PUT");
    }

    @Override
    public boolean ping() {
        try {
            return storage.get(properties.getGoogle().getBucket()) != null
                    || storage.list(properties.getGoogle().getBucket(), Storage.BlobListOption.pageSize(1)) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public MultipartUploadSession beginMultipartUpload(MultipartUploadRequest request) {
        String uploadId = UUID.randomUUID().toString();
        MultipartUploadSession session = new MultipartUploadSession(
                uploadId,
                new StorageObjectRef(request.bucket(), request.key()),
                request.contentType());
        multipartSessions.put(uploadId, session);
        return session;
    }

    @Override
    public MultipartPartResult uploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize) {
        return new MultipartPartResult(partNumber, "gcs-part-" + partNumber, partSize);
    }

    @Override
    public StorageStoreResult completeMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts) {
        multipartSessions.remove(session.uploadId());
        return new StorageStoreResult(
                session.objectRef(),
                headObject(session.objectRef()),
                parts.stream().mapToLong(MultipartPartResult::partSize).sum());
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        multipartSessions.remove(session.uploadId());
    }

    private static Storage buildStorage(DocumentStorageProperties properties) {
        var builder = StorageOptions.newBuilder();
        if (properties.getGoogle().getProjectId() != null && !properties.getGoogle().getProjectId().isBlank()) {
            builder.setProjectId(properties.getGoogle().getProjectId());
        }
        return builder.build().getService();
    }
}
