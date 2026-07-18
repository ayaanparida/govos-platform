package com.govos.doc.storage.provider;

import com.govos.doc.enums.StorageProviderType;
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
import com.govos.doc.storage.support.StorageObjectKeyHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AbstractStorageProvider implements StorageProviderPort {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final StorageMetricsRecorder metricsRecorder;

    protected AbstractStorageProvider(StorageMetricsRecorder metricsRecorder) {
        this.metricsRecorder = metricsRecorder;
    }

    @Override
    public StorageStoreResult store(StorageStoreRequest request, InputStream inputStream) {
        return execute(request.documentId(), request.key(), "store", () -> {
            StorageStoreResult result = doStore(request, inputStream);
            metricsRecorder.recordUpload(providerName(), result.bytesStored());
            return result;
        });
    }

    @Override
    public InputStream load(StorageObjectRef objectRef) {
        long start = System.nanoTime();
        try {
            InputStream stream = doLoad(objectRef);
            log.info(
                    "storage provider={} operation=load keyHash={} durationMs={} status=success",
                    providerName(),
                    StorageObjectKeyHasher.hashKey(objectRef.key()),
                    elapsedMs(start));
            return stream;
        } catch (RuntimeException ex) {
            metricsRecorder.recordFailure(providerName());
            throw ex;
        }
    }

    @Override
    public void delete(StorageObjectRef objectRef) {
        execute(null, objectRef.key(), "delete", () -> {
            doDelete(objectRef);
            metricsRecorder.recordDelete(providerName());
            return null;
        });
    }

    @Override
    public boolean exists(StorageObjectRef objectRef) {
        return doExists(objectRef);
    }

    @Override
    public void copy(StorageObjectRef source, StorageObjectRef destination) {
        execute(null, source.key(), "copy", () -> {
            doCopy(source, destination);
            metricsRecorder.recordCopy(providerName());
            return null;
        });
    }

    @Override
    public void move(StorageObjectRef source, StorageObjectRef destination) {
        execute(null, source.key(), "move", () -> {
            doMove(source, destination);
            metricsRecorder.recordMove(providerName());
            return null;
        });
    }

    @Override
    public SignedUrlResult generateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration) {
        return doGenerateSignedDownloadUrl(objectRef, expiration);
    }

    @Override
    public SignedUrlResult generateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration) {
        return doGenerateSignedUploadUrl(objectRef, expiration);
    }

    @Override
    public StorageObjectMetadata getMetadata(StorageObjectRef objectRef) {
        return doGetMetadata(objectRef);
    }

    @Override
    public List<StorageObjectRef> listObjects(String prefix, int maxKeys) {
        return doListObjects(prefix, maxKeys);
    }

    @Override
    public StorageHealth health() {
        return doHealth();
    }

    @Override
    public MultipartUploadSession beginMultipartUpload(MultipartUploadRequest request) {
        return doBeginMultipartUpload(request);
    }

    @Override
    public MultipartPartResult uploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize) {
        return doUploadPart(session, partNumber, partStream, partSize);
    }

    @Override
    public StorageStoreResult completeMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts) {
        return execute(null, session.objectRef().key(), "completeMultipart", () -> {
            StorageStoreResult result = doCompleteMultipartUpload(session, parts);
            metricsRecorder.recordUpload(providerName(), result.bytesStored());
            return result;
        });
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        doAbortMultipartUpload(session);
    }

    protected abstract StorageStoreResult doStore(StorageStoreRequest request, InputStream inputStream);

    protected abstract InputStream doLoad(StorageObjectRef objectRef);

    protected abstract void doDelete(StorageObjectRef objectRef);

    protected abstract boolean doExists(StorageObjectRef objectRef);

    protected abstract void doCopy(StorageObjectRef source, StorageObjectRef destination);

    protected abstract void doMove(StorageObjectRef source, StorageObjectRef destination);

    protected abstract SignedUrlResult doGenerateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration);

    protected abstract SignedUrlResult doGenerateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration);

    protected abstract StorageObjectMetadata doGetMetadata(StorageObjectRef objectRef);

    protected abstract List<StorageObjectRef> doListObjects(String prefix, int maxKeys);

    protected abstract StorageHealth doHealth();

    protected abstract MultipartUploadSession doBeginMultipartUpload(MultipartUploadRequest request);

    protected abstract MultipartPartResult doUploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize);

    protected abstract StorageStoreResult doCompleteMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts);

    protected abstract void doAbortMultipartUpload(MultipartUploadSession session);

    protected abstract StorageProviderType storageProviderType();

    @Override
    public StorageProviderType providerType() {
        return storageProviderType();
    }

    private <T> T execute(UUID documentId, String key, String operation, Supplier<T> action) {
        long start = System.nanoTime();
        try {
            T result = action.get();
            log.info(
                    "storage provider={} operation={} documentId={} keyHash={} durationMs={} status=success",
                    providerName(),
                    operation,
                    documentId,
                    StorageObjectKeyHasher.hashKey(key),
                    elapsedMs(start));
            return result;
        } catch (RuntimeException ex) {
            metricsRecorder.recordFailure(providerName());
            log.warn(
                    "storage provider={} operation={} documentId={} keyHash={} durationMs={} status=failure",
                    providerName(),
                    operation,
                    documentId,
                    StorageObjectKeyHasher.hashKey(key),
                    elapsedMs(start));
            throw ex instanceof StorageException ? (StorageException) ex : new StorageException(ex.getMessage(), ex);
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }
}
