package com.govos.doc.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;

/**
 * Materialized folder path metadata for hierarchical navigation (DOC-002).
 */
@Embeddable
public class DocumentPath {

    @Size(max = 2048)
    @Column(name = "materialized_path", length = 2048)
    private String materializedPath;

    @Column(name = "depth_level")
    private Integer depthLevel;

    public DocumentPath() {
    }

    public DocumentPath(String materializedPath, Integer depthLevel) {
        this.materializedPath = materializedPath;
        this.depthLevel = depthLevel;
    }

    public String getMaterializedPath() {
        return materializedPath;
    }

    public void setMaterializedPath(String materializedPath) {
        this.materializedPath = materializedPath;
    }

    public Integer getDepthLevel() {
        return depthLevel;
    }

    public void setDepthLevel(Integer depthLevel) {
        this.depthLevel = depthLevel;
    }
}
