package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future SRH search synchronization hook for document lifecycle events.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentSearchIntegration {

    void onDocumentCreated(UUID documentId, UUID organizationId);

    void onDocumentUpdated(UUID documentId, UUID organizationId);

    void onDocumentArchived(UUID documentId, UUID organizationId);

    void onDocumentSoftDeleted(UUID documentId, UUID organizationId);

    void onDocumentRestored(UUID documentId, UUID organizationId);
}
