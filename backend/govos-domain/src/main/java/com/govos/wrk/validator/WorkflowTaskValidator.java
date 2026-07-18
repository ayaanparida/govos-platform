package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowTaskRequest;
import com.govos.wrk.dto.UpdateWorkflowTaskRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WorkflowTaskValidator {

    public void validateCreate(CreateWorkflowTaskRequest request) {
        // No additional business validation beyond request constraints.
    }

    public void validateUpdate(UUID id, UpdateWorkflowTaskRequest request) {
        // No additional business validation beyond request constraints.
    }
}
