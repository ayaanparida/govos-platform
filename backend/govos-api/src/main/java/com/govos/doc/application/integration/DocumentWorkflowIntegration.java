package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future WRK workflow orchestration hook for document approval flows.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentWorkflowIntegration {

    void onDocumentSubmitted(UUID documentId, UUID organizationId, UUID submittedByUserId);

    void onDocumentApproved(UUID documentId, UUID organizationId, UUID approvedByUserId);

    void onDocumentRejected(UUID documentId, UUID organizationId, UUID rejectedByUserId);
}
