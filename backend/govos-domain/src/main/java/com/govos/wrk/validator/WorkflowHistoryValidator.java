package com.govos.wrk.validator;

import com.govos.wrk.dto.CreateWorkflowHistoryRequest;
import org.springframework.stereotype.Component;

@Component
public class WorkflowHistoryValidator {

    public void validateCreate(CreateWorkflowHistoryRequest request) {
        // No additional business validation beyond request constraints.
    }
}
