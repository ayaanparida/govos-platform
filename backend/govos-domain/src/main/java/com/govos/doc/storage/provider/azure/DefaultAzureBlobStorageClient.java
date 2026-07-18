package com.govos.doc.storage.provider.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
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
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultAzureBlobStorageClient implements AzureBlobStorageClient {

    private final BlobServiceClient blobServiceClient;
    private final DocumentStorageProperties properties;
    private final ConcurrentMap<String, MultipartUploadSession> multipartSessions = new ConcurrentHashMap<>();

    public DefaultAzureBlobStorageClient(
            BlobServiceClient blobServiceClient,
            DocumentStorageProperties properties) {
        this.blobServiceClient = blobServiceClient;
        this.properties = properties;
    }

    @Override
    public StorageStoreResult putObject(StorageStoreRequest request, InputStream inputStream) {
        try {
            var blobClient = container(request.bucket()).getBlobClient(request.key());
            if (request.contentLength() > 0) {
                blobClient.upload(inputStream, request.contentLength());
            } else {
                blobClient.upload(inputStream, true);
            }
            StorageObjectMetadata metadata = headObject(new StorageObjectRef(request.bucket(), request.key()));
            return new StorageStoreResult(new StorageObjectRef(request.bucket(), request.key()), metadata, metadata.contentLength());
        } catch (Exception ex) {
            throw new StorageException("Azure putObject failed", ex);
        }
    }

    @Override
    public InputStream getObject(StorageObjectRef objectRef) {
        try {
            return container(objectRef.bucket()).getBlobClient(objectRef.key()).openInputStream();
        } catch (Exception ex) {
            throw new StorageException("Azure getObject failed", ex);
        }
    }

    @Override
    public void deleteObject(StorageObjectRef objectRef) {
        container(objectRef.bucket()).getBlobClient(objectRef.key()).deleteIfExists();
    }

    @Override
    public boolean objectExists(StorageObjectRef objectRef) {
        return container(objectRef.bucket()).getBlobClient(objectRef.key()).exists();
    }

    @Override
    public void copyObject(StorageObjectRef source, StorageObjectRef destination) {
        var sourceClient = container(source.bucket()).getBlobClient(source.key());
        var destinationClient = container(destination.bucket()).getBlobClient(destination.key());
        destinationClient.beginCopy(sourceClient.getBlobUrl(), null);
    }

    @Override
    public StorageObjectMetadata headObject(StorageObjectRef objectRef) {
        BlobProperties props = container(objectRef.bucket()).getBlobClient(objectRef.key()).getProperties();
        return new StorageObjectMetadata(
                props.getContentType(),
                props.getBlobSize(),
                props.getETag(),
                props.getCreationTime() != null ? props.getCreationTime().toInstant() : Instant.now(),
                props.getLastModified().toInstant(),
                props.getAccessTier() != null ? props.getAccessTier().toString() : "STANDARD",
                props.getETag());
    }

    @Override
    public List<StorageObjectRef> listObjects(String container, String prefix, int maxKeys) {
        List<StorageObjectRef> refs = new ArrayList<>();
        for (BlobItem item : container(container).listBlobsByHierarchy(prefix)) {
            refs.add(new StorageObjectRef(container, item.getName()));
            if (refs.size() >= maxKeys) {
                break;
            }
        }
        return refs;
    }

    @Override
    public SignedUrlResult presignGet(StorageObjectRef objectRef, Duration expiration) {
        var blobClient = container(objectRef.bucket()).getBlobClient(objectRef.key());
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plus(expiration),
                BlobSasPermission.parse("r"));
        String sas = blobClient.generateSas(values);
        return new SignedUrlResult(blobClient.getBlobUrl() + "?" + sas, Instant.now().plus(expiration), "GET");
    }

    @Override
    public SignedUrlResult presignPut(StorageObjectRef objectRef, Duration expiration) {
        var blobClient = container(objectRef.bucket()).getBlobClient(objectRef.key());
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
                OffsetDateTime.now().plus(expiration),
                BlobSasPermission.parse("cw"));
        String sas = blobClient.generateSas(values);
        return new SignedUrlResult(blobClient.getBlobUrl() + "?" + sas, Instant.now().plus(expiration), "PUT");
    }

    @Override
    public boolean ping() {
        try {
            return container(properties.getAzure().getContainer()).exists();
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
        return new MultipartPartResult(partNumber, "azure-part-" + partNumber, partSize);
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

    private BlobContainerClient container(String containerName) {
        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
