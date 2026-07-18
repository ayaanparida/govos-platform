package com.govos.doc.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StorageLocationTest {

    @Test
    void shouldCreateWithStorageObjectKeyOnly() {
        StorageLocation location = new StorageLocation("org/file.pdf");

        assertThat(location.getStorageObjectKey()).isEqualTo("org/file.pdf");
        assertThat(location.getPreviewStorageKey()).isNull();
        assertThat(location.getThumbnailStorageKey()).isNull();
    }

    @Test
    void shouldCreateWithAllKeysAndSupportSetters() {
        StorageLocation location = new StorageLocation("org/file.pdf");
        location.setPreviewStorageKey("org/preview");
        location.setThumbnailStorageKey("org/thumb");

        assertThat(location.getPreviewStorageKey()).isEqualTo("org/preview");
        assertThat(location.getThumbnailStorageKey()).isEqualTo("org/thumb");

        location.setStorageObjectKey("updated");
        location.setPreviewStorageKey("preview");
        location.setThumbnailStorageKey("thumb");

        assertThat(location.getStorageObjectKey()).isEqualTo("updated");
        assertThat(location.getPreviewStorageKey()).isEqualTo("preview");
        assertThat(location.getThumbnailStorageKey()).isEqualTo("thumb");
    }
}
