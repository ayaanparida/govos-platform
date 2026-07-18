package com.govos.doc.storage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "govos.document.storage")
public class DocumentStorageProperties {

    private String provider = "local";
    private String basePath = "./data/doc-storage";
    private String bucket = "govos-documents";
    private String region = "us-east-1";
    private long maxFileSize = 5_368_709_120L;
    private long multipartThreshold = 10_485_760L;
    private long signedUrlExpirationSeconds = 300L;
    private int bufferSize = 8192;
    private DocumentStorageMetricsProperties metrics = new DocumentStorageMetricsProperties();
    private LocalStorageProperties local = new LocalStorageProperties();
    private MinioStorageProperties minio = new MinioStorageProperties();
    private S3StorageProperties s3 = new S3StorageProperties();
    private AzureStorageProperties azure = new AzureStorageProperties();
    private GoogleStorageProperties google = new GoogleStorageProperties();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getMultipartThreshold() {
        return multipartThreshold;
    }

    public void setMultipartThreshold(long multipartThreshold) {
        this.multipartThreshold = multipartThreshold;
    }

    public long getSignedUrlExpirationSeconds() {
        return signedUrlExpirationSeconds;
    }

    public void setSignedUrlExpirationSeconds(long signedUrlExpirationSeconds) {
        this.signedUrlExpirationSeconds = signedUrlExpirationSeconds;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public DocumentStorageMetricsProperties getMetrics() {
        return metrics;
    }

    public void setMetrics(DocumentStorageMetricsProperties metrics) {
        this.metrics = metrics;
    }

    public LocalStorageProperties getLocal() {
        return local;
    }

    public void setLocal(LocalStorageProperties local) {
        this.local = local;
    }

    public MinioStorageProperties getMinio() {
        return minio;
    }

    public void setMinio(MinioStorageProperties minio) {
        this.minio = minio;
    }

    public S3StorageProperties getS3() {
        return s3;
    }

    public void setS3(S3StorageProperties s3) {
        this.s3 = s3;
    }

    public AzureStorageProperties getAzure() {
        return azure;
    }

    public void setAzure(AzureStorageProperties azure) {
        this.azure = azure;
    }

    public GoogleStorageProperties getGoogle() {
        return google;
    }

    public void setGoogle(GoogleStorageProperties google) {
        this.google = google;
    }
}
