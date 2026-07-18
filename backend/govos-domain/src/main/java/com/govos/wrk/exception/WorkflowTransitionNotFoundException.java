package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowTransitionNotFoundException extends WrkException {

    public WorkflowTransitionNotFoundException(UUID id) {
        super("Workflow transition not found with id: " + id);
    }
}
