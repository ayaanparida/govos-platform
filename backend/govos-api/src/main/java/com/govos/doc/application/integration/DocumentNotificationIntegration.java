package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future NTF notification hook for document share and lifecycle events.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentNotificationIntegration {

    void onDocumentShared(UUID documentId, UUID shareId, UUID recipientUserId);

    void onShareExpired(UUID documentId, UUID shareId);

    void onShareRevoked(UUID documentId, UUID shareId);
}
