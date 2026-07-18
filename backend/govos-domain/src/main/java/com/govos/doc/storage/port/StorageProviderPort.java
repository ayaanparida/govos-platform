package com.govos.doc.storage.port;

import com.govos.doc.enums.StorageProviderType;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

public interface StorageProviderPort {

    StorageStoreResult store(StorageStoreRequest request, InputStream inputStream);

    InputStream load(StorageObjectRef objectRef);

    void delete(StorageObjectRef objectRef);

    boolean exists(StorageObjectRef objectRef);

    void copy(StorageObjectRef source, StorageObjectRef destination);

    void move(StorageObjectRef source, StorageObjectRef destination);

    SignedUrlResult generateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration);

    SignedUrlResult generateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration);

    StorageObjectMetadata getMetadata(StorageObjectRef objectRef);

    List<StorageObjectRef> listObjects(String prefix, int maxKeys);

    StorageHealth health();

    String providerName();

    StorageProviderType providerType();

    MultipartUploadSession beginMultipartUpload(MultipartUploadRequest request);

    MultipartPartResult uploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize);

    StorageStoreResult completeMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts);

    void abortMultipartUpload(MultipartUploadSession session);
}
