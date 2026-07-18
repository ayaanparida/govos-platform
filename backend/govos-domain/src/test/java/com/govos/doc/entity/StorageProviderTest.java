package com.govos.doc.entity;

import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageProviderTest {

    @Test
    void shouldExposeFixtureDefaults() {
        StorageProvider entity = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        assertThat(entity.getProviderName()).isEqualTo("minio-primary");
        assertThat(entity.getProviderType()).isEqualTo(StorageProviderType.MINIO);
        assertThat(entity.getBucketName()).isEqualTo("govos-documents");
        assertThat(entity.getEndpoint()).isEqualTo("http://localhost:9000");
        assertThat(entity.getRegion()).isEqualTo("local");
        assertThat(entity.getEncryptionEnabled()).isTrue();
        assertThat(entity.getIsDefault()).isTrue();
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void shouldUpdateFieldsViaSetters() {
        StorageProvider entity = new StorageProvider();
        entity.setProviderName("s3-backup");
        entity.setProviderType(StorageProviderType.S3);
        entity.setBucketName("backup");
        entity.setEndpoint("https://s3.amazonaws.com");
        entity.setRegion("us-east-1");
        entity.setEncryptionEnabled(false);
        entity.setIsDefault(false);
        entity.setSecretKeyReference("ref");
        entity.setDeleted(true);
        entity.setVersion(7L);

        assertThat(entity.getProviderName()).isEqualTo("s3-backup");
        assertThat(entity.getProviderType()).isEqualTo(StorageProviderType.S3);
        assertThat(entity.getEncryptionEnabled()).isFalse();
        assertThat(entity.getIsDefault()).isFalse();
        assertThat(entity.getSecretKeyReference()).isEqualTo("ref");
        assertThat(entity.getDeleted()).isTrue();
        assertThat(entity.getVersion()).isEqualTo(7L);
    }
}
