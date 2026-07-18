package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowStepNotFoundException extends WrkException {

    public WorkflowStepNotFoundException(UUID id) {
        super("Workflow step not found with id: " + id);
    }
}
