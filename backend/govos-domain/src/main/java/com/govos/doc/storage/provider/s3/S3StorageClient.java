package com.govos.doc.storage.provider.s3;

import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

public interface S3StorageClient {

    StorageStoreResult putObject(StorageStoreRequest request, InputStream inputStream);

    InputStream getObject(StorageObjectRef objectRef);

    void deleteObject(StorageObjectRef objectRef);

    boolean objectExists(StorageObjectRef objectRef);

    void copyObject(StorageObjectRef source, StorageObjectRef destination);

    StorageObjectMetadata headObject(StorageObjectRef objectRef);

    List<StorageObjectRef> listObjects(String bucket, String prefix, int maxKeys);

    SignedUrlResult presignGet(StorageObjectRef objectRef, Duration expiration);

    SignedUrlResult presignPut(StorageObjectRef objectRef, Duration expiration);

    boolean ping();

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
