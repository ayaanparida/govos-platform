package com.govos.doc.storage.provider.s3;

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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultS3StorageClient implements S3StorageClient {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final DocumentStorageProperties properties;
    private final ConcurrentMap<String, MultipartUploadSession> multipartSessions = new ConcurrentHashMap<>();

    public DefaultS3StorageClient(
            S3Client s3Client,
            S3Presigner s3Presigner,
            DocumentStorageProperties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public StorageStoreResult putObject(StorageStoreRequest request, InputStream inputStream) {
        try {
            long contentLength = request.contentLength() > 0 ? request.contentLength() : -1L;
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(request.bucket())
                            .key(request.key())
                            .contentType(request.contentType())
                            .build(),
                    RequestBody.fromInputStream(inputStream, contentLength));
            StorageObjectMetadata metadata = headObject(new StorageObjectRef(request.bucket(), request.key()));
            return new StorageStoreResult(new StorageObjectRef(request.bucket(), request.key()), metadata, metadata.contentLength());
        } catch (Exception ex) {
            throw new StorageException("S3 putObject failed", ex);
        }
    }

    @Override
    public InputStream getObject(StorageObjectRef objectRef) {
        try {
            return s3Client.getObject(
                    GetObjectRequest.builder().bucket(objectRef.bucket()).key(objectRef.key()).build(),
                    ResponseTransformer.toInputStream());
        } catch (Exception ex) {
            throw new StorageException("S3 getObject failed", ex);
        }
    }

    @Override
    public void deleteObject(StorageObjectRef objectRef) {
        s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(objectRef.bucket()).key(objectRef.key()).build());
    }

    @Override
    public boolean objectExists(StorageObjectRef objectRef) {
        try {
            headObject(objectRef);
            return true;
        } catch (StorageException ex) {
            return false;
        }
    }

    @Override
    public void copyObject(StorageObjectRef source, StorageObjectRef destination) {
        s3Client.copyObject(
                CopyObjectRequest.builder()
                        .sourceBucket(source.bucket())
                        .sourceKey(source.key())
                        .destinationBucket(destination.bucket())
                        .destinationKey(destination.key())
                        .build());
    }

    @Override
    public StorageObjectMetadata headObject(StorageObjectRef objectRef) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(objectRef.bucket()).key(objectRef.key()).build());
            return new StorageObjectMetadata(
                    response.contentType(),
                    response.contentLength(),
                    response.eTag(),
                    response.lastModified(),
                    response.lastModified(),
                    response.storageClassAsString(),
                    response.eTag());
        } catch (NoSuchKeyException ex) {
            throw new StorageException("S3 object not found: " + objectRef.key(), ex);
        }
    }

    @Override
    public List<StorageObjectRef> listObjects(String bucket, String prefix, int maxKeys) {
        var response = s3Client.listObjectsV2(
                ListObjectsV2Request.builder().bucket(bucket).prefix(prefix).maxKeys(maxKeys).build());
        List<StorageObjectRef> refs = new ArrayList<>();
        for (S3Object object : response.contents()) {
            refs.add(new StorageObjectRef(bucket, object.key()));
        }
        return refs;
    }

    @Override
    public SignedUrlResult presignGet(StorageObjectRef objectRef, Duration expiration) {
        var presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .getObjectRequest(builder -> builder.bucket(objectRef.bucket()).key(objectRef.key()))
                        .build());
        return new SignedUrlResult(presigned.url().toString(), Instant.now().plus(expiration), "GET");
    }

    @Override
    public SignedUrlResult presignPut(StorageObjectRef objectRef, Duration expiration) {
        var presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(expiration)
                        .putObjectRequest(builder -> builder.bucket(objectRef.bucket()).key(objectRef.key()))
                        .build());
        return new SignedUrlResult(presigned.url().toString(), Instant.now().plus(expiration), "PUT");
    }

    @Override
    public boolean ping() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.getS3().getBucket()).build());
            return true;
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
        return new MultipartPartResult(partNumber, "s3-part-" + partNumber, partSize);
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
}
