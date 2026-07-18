package com.govos.doc.application.integration;

import java.util.UUID;

/**
 * Future object storage hook for upload, download, and delete operations.
 * Implementation deferred to a later DOC sprint.
 */
public interface DocumentStorageIntegration {

    void onObjectStored(UUID documentVersionId, UUID storageProviderId, String storageObjectKey);

    void onObjectDeleted(UUID documentVersionId, UUID storageProviderId, String storageObjectKey);
}
