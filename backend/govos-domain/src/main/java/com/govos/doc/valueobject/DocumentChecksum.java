package com.govos.doc.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * SHA-256 checksum for an immutable document version (DOC-002).
 */
@Embeddable
public class DocumentChecksum {

    @NotBlank
    @Size(max = 128)
    @Column(name = "checksum", nullable = false, length = 128)
    private String value;

    public DocumentChecksum() {
    }

    public DocumentChecksum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
