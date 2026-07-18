package com.govos.doc.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Positive;

/**
 * File size in bytes for a document version (DOC-002).
 */
@Embeddable
public class FileSize {

    @Positive
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    public FileSize() {
    }

    public FileSize(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(Long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
}
