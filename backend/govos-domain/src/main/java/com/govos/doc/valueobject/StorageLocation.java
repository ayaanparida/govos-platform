package com.govos.doc.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Physical storage coordinates for a document version blob (DOC-002).
 */
@Embeddable
public class StorageLocation {

    @NotBlank
    @Size(max = 1024)
    @Column(name = "storage_object_key", nullable = false, length = 1024)
    private String storageObjectKey;

    @Size(max = 255)
    @Column(name = "preview_storage_key", length = 1024)
    private String previewStorageKey;

    @Size(max = 255)
    @Column(name = "thumbnail_storage_key", length = 1024)
    private String thumbnailStorageKey;

    public StorageLocation() {
    }

    public StorageLocation(String storageObjectKey) {
        this.storageObjectKey = storageObjectKey;
    }

    public String getStorageObjectKey() {
        return storageObjectKey;
    }

    public void setStorageObjectKey(String storageObjectKey) {
        this.storageObjectKey = storageObjectKey;
    }

    public String getPreviewStorageKey() {
        return previewStorageKey;
    }

    public void setPreviewStorageKey(String previewStorageKey) {
        this.previewStorageKey = previewStorageKey;
    }

    public String getThumbnailStorageKey() {
        return thumbnailStorageKey;
    }

    public void setThumbnailStorageKey(String thumbnailStorageKey) {
        this.thumbnailStorageKey = thumbnailStorageKey;
    }
}
