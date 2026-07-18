package com.govos.doc.event;

/**
 * Canonical event type identifiers for DOC domain events.
 */
public final class DocumentEventTypes {

    public static final String DOCUMENT_CREATED = "DocumentCreated";
    public static final String DOCUMENT_UPDATED = "DocumentUpdated";
    public static final String DOCUMENT_DELETED = "DocumentDeleted";
    public static final String DOCUMENT_RESTORED = "DocumentRestored";
    public static final String DOCUMENT_ARCHIVED = "DocumentArchived";
    public static final String DOCUMENT_MOVED = "DocumentMoved";
    public static final String DOCUMENT_RENAMED = "DocumentRenamed";
    public static final String DOCUMENT_CLASSIFICATION_CHANGED = "DocumentClassificationChanged";
    public static final String DOCUMENT_ACTIVE_VERSION_CHANGED = "DocumentActiveVersionChanged";

    public static final String DOCUMENT_VERSION_CREATED = "DocumentVersionCreated";
    public static final String DOCUMENT_VERSION_ACTIVATED = "DocumentVersionActivated";

    public static final String FOLDER_CREATED = "FolderCreated";
    public static final String FOLDER_UPDATED = "FolderUpdated";
    public static final String FOLDER_MOVED = "FolderMoved";
    public static final String FOLDER_DELETED = "FolderDeleted";
    public static final String FOLDER_RESTORED = "FolderRestored";

    public static final String DOCUMENT_CATEGORY_CREATED = "DocumentCategoryCreated";
    public static final String DOCUMENT_CATEGORY_UPDATED = "DocumentCategoryUpdated";
    public static final String DOCUMENT_CATEGORY_DELETED = "DocumentCategoryDeleted";
    public static final String DOCUMENT_CATEGORY_RESTORED = "DocumentCategoryRestored";

    public static final String DOCUMENT_METADATA_CREATED = "DocumentMetadataCreated";
    public static final String DOCUMENT_METADATA_UPDATED = "DocumentMetadataUpdated";

    public static final String RETENTION_POLICY_CREATED = "RetentionPolicyCreated";
    public static final String RETENTION_POLICY_UPDATED = "RetentionPolicyUpdated";
    public static final String RETENTION_POLICY_DELETED = "RetentionPolicyDeleted";
    public static final String RETENTION_POLICY_RESTORED = "RetentionPolicyRestored";

    public static final String DOCUMENT_SHARED = "DocumentShared";
    public static final String DOCUMENT_SHARE_EXPIRED = "DocumentShareExpired";
    public static final String DOCUMENT_SHARE_REVOKED = "DocumentShareRevoked";

    public static final String STORAGE_PROVIDER_CREATED = "StorageProviderCreated";
    public static final String STORAGE_PROVIDER_UPDATED = "StorageProviderUpdated";
    public static final String STORAGE_PROVIDER_ACTIVATED = "StorageProviderActivated";
    public static final String STORAGE_PROVIDER_DEACTIVATED = "StorageProviderDeactivated";
    public static final String STORAGE_PROVIDER_DEFAULT_CHANGED = "StorageProviderDefaultChanged";

    private DocumentEventTypes() {
    }
}
