package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future preview generation hook for document versions.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentPreviewIntegration {

    void onPreviewRequested(UUID documentVersionId, UUID documentId);
}
