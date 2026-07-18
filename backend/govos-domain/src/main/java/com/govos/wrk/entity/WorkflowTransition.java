package com.govos.wrk.entity;

import com.govos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wrk_workflow_transition", schema = "govos")
public class WorkflowTransition extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_step_id", nullable = false)
    private WorkflowStep fromStep;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_step_id", nullable = false)
    private WorkflowStep toStep;

    @Column(name = "condition_expression", length = 2000)
    private String conditionExpression;

    public WorkflowStep getFromStep() {
        return fromStep;
    }

    public void setFromStep(WorkflowStep fromStep) {
        this.fromStep = fromStep;
    }

    public WorkflowStep getToStep() {
        return toStep;
    }

    public void setToStep(WorkflowStep toStep) {
        this.toStep = toStep;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }
}
