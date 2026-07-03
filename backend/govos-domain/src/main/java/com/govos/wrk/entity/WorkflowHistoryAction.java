package com.govos.wrk.entity;

public enum WorkflowHistoryAction {
    INSTANCE_STARTED,
    INSTANCE_COMPLETED,
    INSTANCE_CANCELLED,
    TASK_CREATED,
    TASK_ASSIGNED,
    TASK_COMPLETED,
    STEP_ENTERED,
    STEP_EXITED,
    TRANSITION
}
