package com.govos.doc.storage.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.govos.doc.storage.provider.azure.AzureBlobStorageClient;
import com.govos.doc.storage.provider.azure.DefaultAzureBlobStorageClient;
import com.govos.doc.storage.provider.gcs.DefaultGoogleCloudStorageClient;
import com.govos.doc.storage.provider.gcs.GoogleCloudStorageClient;
import com.govos.doc.storage.provider.minio.DefaultMinioStorageClient;
import com.govos.doc.storage.provider.minio.MinioStorageClient;
import com.govos.doc.storage.provider.s3.DefaultS3StorageClient;
import com.govos.doc.storage.provider.s3.S3StorageClient;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(DocumentStorageProperties.class)
public class DocStorageConfiguration {

    @Bean
    public MinioClient minioClient(DocumentStorageProperties properties) {
        var minio = properties.getMinio();
        if (minio.getAccessKey() == null || minio.getAccessKey().isBlank()) {
            return null;
        }
        return MinioClient.builder()
                .endpoint(minio.getEndpoint())
                .credentials(minio.getAccessKey(), minio.getSecretKey())
                .build();
    }

    @Bean
    public MinioStorageClient minioStorageClient(
            DocumentStorageProperties properties,
            org.springframework.beans.factory.ObjectProvider<MinioClient> minioClientProvider) {
        MinioClient client = minioClientProvider.getIfAvailable();
        if (client == null) {
            return new UnconfiguredMinioStorageClient();
        }
        return new DefaultMinioStorageClient(client, properties);
    }

    @Bean
    public S3Client s3Client(DocumentStorageProperties properties) {
        var s3 = properties.getS3();
        if (s3.getAccessKey() == null || s3.getAccessKey().isBlank()) {
            return null;
        }
        return S3Client.builder()
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(DocumentStorageProperties properties) {
        var s3 = properties.getS3();
        if (s3.getAccessKey() == null || s3.getAccessKey().isBlank()) {
            return null;
        }
        return S3Presigner.builder()
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3.getAccessKey(), s3.getSecretKey())))
                .build();
    }

    @Bean
    public S3StorageClient s3StorageClient(
            DocumentStorageProperties properties,
            org.springframework.beans.factory.ObjectProvider<S3Client> s3ClientProvider,
            org.springframework.beans.factory.ObjectProvider<S3Presigner> s3PresignerProvider) {
        S3Client client = s3ClientProvider.getIfAvailable();
        S3Presigner presigner = s3PresignerProvider.getIfAvailable();
        if (client == null || presigner == null) {
            return new UnconfiguredS3StorageClient();
        }
        return new DefaultS3StorageClient(client, presigner, properties);
    }

    @Bean
    public BlobServiceClient blobServiceClient(DocumentStorageProperties properties) {
        String connectionString = properties.getAzure().getConnectionString();
        if (connectionString == null || connectionString.isBlank()) {
            return null;
        }
        return new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }

    @Bean
    public AzureBlobStorageClient azureBlobStorageClient(
            DocumentStorageProperties properties,
            org.springframework.beans.factory.ObjectProvider<BlobServiceClient> blobServiceClientProvider) {
        BlobServiceClient client = blobServiceClientProvider.getIfAvailable();
        if (client == null) {
            return new UnconfiguredAzureBlobStorageClient();
        }
        return new DefaultAzureBlobStorageClient(client, properties);
    }

    @Bean
    public Storage googleCloudStorage(DocumentStorageProperties properties) {
        if (properties.getGoogle().getProjectId() == null || properties.getGoogle().getProjectId().isBlank()) {
            return null;
        }
        return StorageOptions.newBuilder()
                .setProjectId(properties.getGoogle().getProjectId())
                .build()
                .getService();
    }

    @Bean
    public GoogleCloudStorageClient googleCloudStorageClient(
            DocumentStorageProperties properties,
            org.springframework.beans.factory.ObjectProvider<Storage> storageProvider) {
        Storage storage = storageProvider.getIfAvailable();
        if (storage == null) {
            return new UnconfiguredGoogleCloudStorageClient();
        }
        return new DefaultGoogleCloudStorageClient(storage, properties);
    }
}
