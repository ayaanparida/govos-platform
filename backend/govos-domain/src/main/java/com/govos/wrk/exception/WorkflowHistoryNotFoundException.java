package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowHistoryNotFoundException extends WrkException {

    public WorkflowHistoryNotFoundException(UUID id) {
        super("Workflow history not found with id: " + id);
    }
}
