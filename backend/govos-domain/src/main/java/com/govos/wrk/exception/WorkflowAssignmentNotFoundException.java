package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowAssignmentNotFoundException extends WrkException {

    public WorkflowAssignmentNotFoundException(UUID id) {
        super("Workflow assignment not found with id: " + id);
    }
}
