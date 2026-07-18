package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future AUD audit hook for document access and mutation events.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentAuditIntegration {

    void onDocumentCreated(UUID documentId, UUID organizationId, UUID actorUserId);

    void onDocumentUpdated(UUID documentId, UUID organizationId, UUID actorUserId);

    void onDocumentAccessed(UUID documentId, UUID organizationId, UUID actorUserId, String operation);
}
