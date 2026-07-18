package com.govos.doc.mapper;

import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageProviderMapperTest {

    private StorageProviderMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new StorageProviderMapperImpl();
    }

    @Test
    void shouldMapEntityToResponse() {
        StorageProvider entity = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        var response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(DocumentTestFixtures.PROVIDER_ID);
        assertThat(response.providerName()).isEqualTo("minio-primary");
        assertThat(response.providerType()).isEqualTo(StorageProviderType.MINIO);
        assertThat(response.isDefault()).isTrue();
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        CreateStorageProviderRequest request = DocumentTestFixtures.createStorageProviderRequest();

        StorageProvider entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getProviderName()).isEqualTo("minio-primary");
        assertThat(entity.getBucketName()).isEqualTo("govos-documents");
        assertThat(entity.getIsDefault()).isFalse();
    }

    @Test
    void shouldUpdateEntityIgnoringProviderNameWhenNull() {
        StorageProvider entity = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                null, null, "new-bucket", null, null, false, true, "secret", false, 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getProviderName()).isEqualTo("minio-primary");
        assertThat(entity.getBucketName()).isEqualTo("new-bucket");
        assertThat(entity.getEncryptionEnabled()).isFalse();
        assertThat(entity.getIsDefault()).isTrue();
    }

    @Test
    void shouldMapAllUpdateFieldsWhenProvided() {
        StorageProvider entity = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        UpdateStorageProviderRequest request = new UpdateStorageProviderRequest(
                "renamed", StorageProviderType.S3, "new-bucket", "https://s3.amazonaws.com",
                "us-east-1", false, false, "new-secret", false, 0L);
        mapper.updateEntity(request, entity);

        assertThat(entity.getProviderName()).isEqualTo("renamed");
        assertThat(entity.getProviderType()).isEqualTo(StorageProviderType.S3);
        assertThat(entity.getEndpoint()).isEqualTo("https://s3.amazonaws.com");
        assertThat(entity.getRegion()).isEqualTo("us-east-1");
        assertThat(entity.getIsDefault()).isFalse();
    }

    @Test
    void shouldMapResponseListAndHandleNullEntity() {
        StorageProvider entity = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        assertThat(mapper.toResponseList(java.util.List.of(entity))).hasSize(1);
        assertThat(mapper.toResponse(null)).isNull();
    }
}
