package com.govos.doc.storage.provider.minio;

import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.config.MinioStorageProperties;
import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageException;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import io.minio.messages.Item;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultMinioStorageClient implements MinioStorageClient {

    private final MinioClient minioClient;
    private final DocumentStorageProperties properties;
    private final ConcurrentMap<String, MultipartUploadSession> multipartSessions = new ConcurrentHashMap<>();

    public DefaultMinioStorageClient(MinioClient minioClient, DocumentStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public StorageStoreResult putObject(StorageStoreRequest request, InputStream inputStream) {
        try {
            ensureBucket(request.bucket());
            ObjectWriteResponse response = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(request.bucket())
                            .object(request.key())
                            .stream(inputStream, request.contentLength() > 0 ? request.contentLength() : -1, properties.getBufferSize())
                            .contentType(request.contentType())
                            .build());
            StorageObjectMetadata metadata = headObject(new StorageObjectRef(request.bucket(), request.key()));
            return new StorageStoreResult(
                    new StorageObjectRef(request.bucket(), request.key(), response.versionId()),
                    metadata,
                    metadata.contentLength());
        } catch (Exception ex) {
            throw new StorageException("MinIO putObject failed", ex);
        }
    }

    @Override
    public InputStream getObject(StorageObjectRef objectRef) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(objectRef.bucket())
                            .object(objectRef.key())
                            .build());
        } catch (Exception ex) {
            throw new StorageException("MinIO getObject failed", ex);
        }
    }

    @Override
    public void deleteObject(StorageObjectRef objectRef) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(objectRef.bucket())
                            .object(objectRef.key())
                            .build());
        } catch (Exception ex) {
            throw new StorageException("MinIO deleteObject failed", ex);
        }
    }

    @Override
    public boolean objectExists(StorageObjectRef objectRef) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(objectRef.bucket())
                            .object(objectRef.key())
                            .build());
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void copyObject(StorageObjectRef source, StorageObjectRef destination) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(destination.bucket())
                            .object(destination.key())
                            .source(CopySource.builder().bucket(source.bucket()).object(source.key()).build())
                            .build());
        } catch (Exception ex) {
            throw new StorageException("MinIO copyObject failed", ex);
        }
    }

    @Override
    public StorageObjectMetadata headObject(StorageObjectRef objectRef) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(objectRef.bucket())
                            .object(objectRef.key())
                            .build());
            return new StorageObjectMetadata(
                    stat.contentType(),
                    stat.size(),
                    stat.etag(),
                    stat.lastModified().toInstant(),
                    stat.lastModified().toInstant(),
                    "STANDARD",
                    stat.etag());
        } catch (Exception ex) {
            throw new StorageException("MinIO headObject failed", ex);
        }
    }

    @Override
    public List<StorageObjectRef> listObjects(String bucket, String prefix, int maxKeys) {
        try {
            List<StorageObjectRef> refs = new ArrayList<>();
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucket).prefix(prefix).maxKeys(maxKeys).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                refs.add(new StorageObjectRef(bucket, item.objectName()));
            }
            return refs;
        } catch (Exception ex) {
            throw new StorageException("MinIO listObjects failed", ex);
        }
    }

    @Override
    public SignedUrlResult presignGet(StorageObjectRef objectRef, Duration expiration) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(objectRef.bucket())
                            .object(objectRef.key())
                            .expiry((int) expiration.toSeconds())
                            .build());
            return new SignedUrlResult(url, Instant.now().plus(expiration), "GET");
        } catch (Exception ex) {
            throw new StorageException("MinIO presign GET failed", ex);
        }
    }

    @Override
    public SignedUrlResult presignPut(StorageObjectRef objectRef, Duration expiration) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(objectRef.bucket())
                            .object(objectRef.key())
                            .expiry((int) expiration.toSeconds())
                            .build());
            return new SignedUrlResult(url, Instant.now().plus(expiration), "PUT");
        } catch (Exception ex) {
            throw new StorageException("MinIO presign PUT failed", ex);
        }
    }

    @Override
    public boolean ping() {
        try {
            MinioStorageProperties minio = properties.getMinio();
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(minio.getBucket()).build());
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
        return new MultipartPartResult(partNumber, "minio-part-" + partNumber, partSize);
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

    private void ensureBucket(String bucket) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
