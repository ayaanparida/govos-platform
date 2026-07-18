package com.govos.wrk.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wrk_workflow_version", schema = "govos")
public class WorkflowVersion extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinition definition;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "published", nullable = false)
    private Boolean published = false;

    public WorkflowDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(WorkflowDefinition definition) {
        this.definition = definition;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }
}
