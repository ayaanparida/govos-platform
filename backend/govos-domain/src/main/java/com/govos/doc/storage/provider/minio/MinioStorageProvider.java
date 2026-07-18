package com.govos.doc.storage.provider.minio;

import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.storage.config.DocumentStorageProperties;
import com.govos.doc.storage.metrics.StorageMetricsRecorder;
import com.govos.doc.storage.port.MultipartPartResult;
import com.govos.doc.storage.port.MultipartUploadRequest;
import com.govos.doc.storage.port.MultipartUploadSession;
import com.govos.doc.storage.port.SignedUrlResult;
import com.govos.doc.storage.port.StorageHealth;
import com.govos.doc.storage.port.StorageObjectMetadata;
import com.govos.doc.storage.port.StorageObjectRef;
import com.govos.doc.storage.port.StorageStoreRequest;
import com.govos.doc.storage.port.StorageStoreResult;
import com.govos.doc.storage.provider.AbstractStorageProvider;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

@Component
public class MinioStorageProvider extends AbstractStorageProvider {

    private final MinioStorageClient client;
    private final DocumentStorageProperties properties;

    public MinioStorageProvider(
            MinioStorageClient client,
            DocumentStorageProperties properties,
            StorageMetricsRecorder metricsRecorder) {
        super(metricsRecorder);
        this.client = client;
        this.properties = properties;
    }

    @Override
    public String providerName() {
        return "minio";
    }

    @Override
    protected StorageProviderType storageProviderType() {
        return StorageProviderType.MINIO;
    }

    @Override
    protected StorageStoreResult doStore(StorageStoreRequest request, InputStream inputStream) {
        return client.putObject(request, inputStream);
    }

    @Override
    protected InputStream doLoad(StorageObjectRef objectRef) {
        return client.getObject(objectRef);
    }

    @Override
    protected void doDelete(StorageObjectRef objectRef) {
        client.deleteObject(objectRef);
    }

    @Override
    protected boolean doExists(StorageObjectRef objectRef) {
        return client.objectExists(objectRef);
    }

    @Override
    protected void doCopy(StorageObjectRef source, StorageObjectRef destination) {
        client.copyObject(source, destination);
    }

    @Override
    protected void doMove(StorageObjectRef source, StorageObjectRef destination) {
        client.copyObject(source, destination);
        client.deleteObject(source);
    }

    @Override
    protected SignedUrlResult doGenerateSignedDownloadUrl(StorageObjectRef objectRef, Duration expiration) {
        return client.presignGet(objectRef, expiration);
    }

    @Override
    protected SignedUrlResult doGenerateSignedUploadUrl(StorageObjectRef objectRef, Duration expiration) {
        return client.presignPut(objectRef, expiration);
    }

    @Override
    protected StorageObjectMetadata doGetMetadata(StorageObjectRef objectRef) {
        return client.headObject(objectRef);
    }

    @Override
    protected List<StorageObjectRef> doListObjects(String prefix, int maxKeys) {
        return client.listObjects(properties.getMinio().getBucket(), prefix, maxKeys);
    }

    @Override
    protected StorageHealth doHealth() {
        return client.ping() ? StorageHealth.up() : StorageHealth.degraded("MinIO bucket is unavailable");
    }

    @Override
    protected MultipartUploadSession doBeginMultipartUpload(MultipartUploadRequest request) {
        return client.beginMultipartUpload(request);
    }

    @Override
    protected MultipartPartResult doUploadPart(
            MultipartUploadSession session,
            int partNumber,
            InputStream partStream,
            long partSize) {
        return client.uploadPart(session, partNumber, partStream, partSize);
    }

    @Override
    protected StorageStoreResult doCompleteMultipartUpload(
            MultipartUploadSession session,
            List<MultipartPartResult> parts) {
        return client.completeMultipartUpload(session, parts);
    }

    @Override
    protected void doAbortMultipartUpload(MultipartUploadSession session) {
        client.abortMultipartUpload(session);
    }
}
