package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future OCR pipeline hook for text extraction requests.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentOcrIntegration {

    void onOcrRequested(UUID documentVersionId, UUID documentId, String language);
}
