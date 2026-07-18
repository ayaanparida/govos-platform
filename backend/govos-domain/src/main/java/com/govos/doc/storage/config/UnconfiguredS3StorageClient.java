package com.govos.doc.storage.config;

import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageException;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import com.govos.doc.storage.provider.s3.S3StorageClient;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

final class UnconfiguredS3StorageClient implements S3StorageClient {

    @Override
    public StorageStoreResult putObject(StorageStoreRequest request, InputStream inputStream) {
        throw unconfigured();
    }

    @Override
    public InputStream getObject(StorageObjectRef objectRef) {
        throw unconfigured();
    }

    @Override
    public void deleteObject(StorageObjectRef objectRef) {
        throw unconfigured();
    }

    @Override
    public boolean objectExists(StorageObjectRef objectRef) {
        return false;
    }

    @Override
    public void copyObject(StorageObjectRef source, StorageObjectRef destination) {
        throw unconfigured();
    }

    @Override
    public StorageObjectMetadata headObject(StorageObjectRef objectRef) {
        throw unconfigured();
    }

    @Override
    public List<StorageObjectRef> listObjects(String bucket, String prefix, int maxKeys) {
        return List.of();
    }

    @Override
    public SignedUrlResult presignGet(StorageObjectRef objectRef, Duration expiration) {
        throw unconfigured();
    }

    @Override
    public SignedUrlResult presignPut(StorageObjectRef objectRef, Duration expiration) {
        throw unconfigured();
    }

    @Override
    public boolean ping() {
        return false;
    }

    @Override
    public MultipartUploadSession beginMultipartUpload(MultipartUploadRequest request) {
        throw unconfigured();
    }

    @Override
    public MultipartPartResult uploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize) {
        throw unconfigured();
    }

    @Override
    public StorageStoreResult completeMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts) {
        throw unconfigured();
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        throw unconfigured();
    }

    private StorageException unconfigured() {
        return new StorageException("S3 storage client is not configured");
    }
}
