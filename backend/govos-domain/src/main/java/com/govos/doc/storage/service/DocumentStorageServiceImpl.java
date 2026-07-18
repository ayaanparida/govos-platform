package com.govos.doc.storage.service;

import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.factory.StorageProviderFactory;
import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageProviderPort;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import com.govos.doc.storage.support.StorageObjectKeyHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentStorageServiceImpl implements DocumentStorageService {

    private static final Logger log = LoggerFactory.getLogger(DocumentStorageServiceImpl.class);

    private final StorageProviderFactory storageProviderFactory;
    private final DocumentStorageProperties properties;

    public DocumentStorageServiceImpl(
            StorageProviderFactory storageProviderFactory,
            DocumentStorageProperties properties) {
        this.storageProviderFactory = storageProviderFactory;
        this.properties = properties;
    }

    @Override
    public StorageStoreResult storeDocument(StorageStoreRequest request, InputStream inputStream) {
        validateFileSize(request.contentLength());
        return activeProvider().store(request, inputStream);
    }

    @Override
    public InputStream loadDocument(UUID documentId, StorageObjectRef objectRef) {
        log.debug(
                "storage_service operation=load documentId={} provider={} keyHash={}",
                documentId,
                activeProvider().providerName(),
                StorageObjectKeyHasher.hashKey(objectRef.key()));
        return activeProvider().load(objectRef);
    }

    @Override
    public void deleteDocument(UUID documentId, StorageObjectRef objectRef) {
        log.debug(
                "storage_service operation=delete documentId={} provider={} keyHash={}",
                documentId,
                activeProvider().providerName(),
                StorageObjectKeyHasher.hashKey(objectRef.key()));
        activeProvider().delete(objectRef);
    }

    @Override
    public boolean exists(StorageObjectRef objectRef) {
        return activeProvider().exists(objectRef);
    }

    @Override
    public void copy(StorageObjectRef source, StorageObjectRef destination) {
        activeProvider().copy(source, destination);
    }

    @Override
    public void move(StorageObjectRef source, StorageObjectRef destination) {
        activeProvider().move(source, destination);
    }

    @Override
    public SignedUrlResult generateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration) {
        Duration ttl = expiration != null ? expiration : defaultSignedUrlTtl();
        return activeProvider().generateSignedDownloadUrl(objectRef, ttl);
    }

    @Override
    public SignedUrlResult generateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration) {
        Duration ttl = expiration != null ? expiration : defaultSignedUrlTtl();
        return activeProvider().generateSignedUploadUrl(objectRef, ttl);
    }

    @Override
    public StorageObjectMetadata getMetadata(StorageObjectRef objectRef) {
        return activeProvider().getMetadata(objectRef);
    }

    @Override
    public List<StorageObjectRef> listObjects(String prefix, int maxKeys) {
        return activeProvider().listObjects(prefix, maxKeys);
    }

    @Override
    public StorageHealth health() {
        return activeProvider().health();
    }

    @Override
    public MultipartUploadSession beginMultipartUpload(MultipartUploadRequest request) {
        return activeProvider().beginMultipartUpload(request);
    }

    @Override
    public MultipartPartResult uploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize) {
        return activeProvider().uploadPart(session, partNumber, partStream, partSize);
    }

    @Override
    public StorageStoreResult completeMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts) {
        return activeProvider().completeMultipartUpload(session, parts);
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        activeProvider().abortMultipartUpload(session);
    }

    private StorageProviderPort activeProvider() {
        return storageProviderFactory.resolveActiveProvider();
    }

    private Duration defaultSignedUrlTtl() {
        return Duration.ofSeconds(properties.getSignedUrlExpirationSeconds());
    }

    private void validateFileSize(long contentLength) {
        if (contentLength > 0 && contentLength > properties.getMaxFileSize()) {
            throw new com.govos.doc.storage.port.StorageException(
                    "File exceeds configured max-file-size limit");
        }
    }
}
