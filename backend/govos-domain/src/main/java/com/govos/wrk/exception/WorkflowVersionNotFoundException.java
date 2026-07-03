package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowVersionNotFoundException extends WrkException {

    public WorkflowVersionNotFoundException(UUID id) {
        super("Workflow version not found with id: " + id);
    }
}
