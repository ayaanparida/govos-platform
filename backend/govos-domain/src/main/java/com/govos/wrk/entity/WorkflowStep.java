package com.govos.wrk.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wrk_workflow_step", schema = "govos")
public class WorkflowStep extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersion workflowVersion;

    @Column(name = "step_name", nullable = false, length = 255)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false, length = 30)
    private WorkflowStepType stepType;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;

    @Column(name = "sla_hours")
    private Integer slaHours;

    public WorkflowVersion getWorkflowVersion() {
        return workflowVersion;
    }

    public void setWorkflowVersion(WorkflowVersion workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public WorkflowStepType getStepType() {
        return stepType;
    }

    public void setStepType(WorkflowStepType stepType) {
        this.stepType = stepType;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Integer getSlaHours() {
        return slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }
}
