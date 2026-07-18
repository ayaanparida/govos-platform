package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowTaskNotFoundException extends WrkException {

    public WorkflowTaskNotFoundException(UUID id) {
        super("Workflow task not found with id: " + id);
    }
}
