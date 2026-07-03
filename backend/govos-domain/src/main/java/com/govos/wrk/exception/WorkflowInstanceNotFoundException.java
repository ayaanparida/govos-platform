package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowInstanceNotFoundException extends WrkException {

    public WorkflowInstanceNotFoundException(UUID id) {
        super("Workflow instance not found with id: " + id);
    }
}
