package com.govos.wrk.exception;

import java.util.UUID;

public class WorkflowDefinitionNotFoundException extends WrkException {

    public WorkflowDefinitionNotFoundException(UUID id) {
        super("Workflow definition not found with id: " + id);
    }

    public WorkflowDefinitionNotFoundException(String code) {
        super("Workflow definition not found with code: " + code);
    }
}
