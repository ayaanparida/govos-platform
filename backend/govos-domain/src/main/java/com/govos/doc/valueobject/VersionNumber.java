package com.govos.doc.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Monotonic version number and optional display label (DOC-002).
 */
@Embeddable
public class VersionNumber {

    @NotNull
    @Positive
    @Column(name = "version_number", nullable = false)
    private Integer value;

    @Size(max = 20)
    @Column(name = "version_label", length = 20)
    private String label;

    public VersionNumber() {
    }

    public VersionNumber(Integer value) {
        this.value = value;
    }

    public VersionNumber(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
