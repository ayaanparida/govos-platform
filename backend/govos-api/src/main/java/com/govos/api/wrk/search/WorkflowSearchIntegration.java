package com.govos.api.wrk.search;

import java.util.UUID;

/**
 * SRH-013 extension point for WRK product search synchronization.
 * Implementation deferred until workflow instances are indexed through {@code SearchApplicationService}.
 */
public interface WorkflowSearchIntegration {

    void onWorkflowInstanceCreated(UUID workflowInstanceId, UUID organizationId);

    void onWorkflowInstanceUpdated(UUID workflowInstanceId, UUID organizationId);

    void onWorkflowInstanceCompleted(UUID workflowInstanceId, UUID organizationId);

    void onWorkflowInstanceArchived(UUID workflowInstanceId, UUID organizationId);
}
