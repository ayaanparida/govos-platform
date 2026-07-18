package com.govos.doc.storage.service;

import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface DocumentStorageService {

    StorageStoreResult storeDocument(StorageStoreRequest request, InputStream inputStream);

    InputStream loadDocument(UUID documentId, StorageObjectRef objectRef);

    void deleteDocument(UUID documentId, StorageObjectRef objectRef);

    boolean exists(StorageObjectRef objectRef);

    void copy(StorageObjectRef source, StorageObjectRef destination);

    void move(StorageObjectRef source, StorageObjectRef destination);

    SignedUrlResult generateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration);

    SignedUrlResult generateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration);

    StorageObjectMetadata getMetadata(StorageObjectRef objectRef);

    List<StorageObjectRef> listObjects(String prefix, int maxKeys);

    StorageHealth health();

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
