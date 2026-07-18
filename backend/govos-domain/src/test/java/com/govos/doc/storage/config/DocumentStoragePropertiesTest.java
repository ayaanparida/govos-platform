package com.govos.doc.storage.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentStoragePropertiesTest {

    @Test
    void shouldBindDefaults() {
        DocumentStorageProperties properties = new DocumentStorageProperties();

        assertThat(properties.getProvider()).isEqualTo("local");
        assertThat(properties.getBucket()).isEqualTo("govos-documents");
        assertThat(properties.getMaxFileSize()).isEqualTo(5_368_709_120L);
        assertThat(properties.getMultipartThreshold()).isEqualTo(10_485_760L);
        assertThat(properties.getSignedUrlExpirationSeconds()).isEqualTo(300L);
        assertThat(properties.getMetrics().isEnabled()).isTrue();
    }
}
