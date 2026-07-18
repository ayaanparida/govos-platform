package com.govos.doc.event;

import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentMetadata;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.entity.DocumentShare;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.Folder;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.DocumentClassification;

import java.time.Instant;
import java.util.UUID;

/**
 * Factory helpers for building immutable DOC domain events from persisted state (DOC-008).
 */
public final class DocumentEvents {

    private DocumentEvents() {
    }

    public static DocumentCreatedEvent documentCreated(Document document) {
        return new DocumentCreatedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                document.getTitle(),
                document.getDocumentNumber(),
                document.getStatus(),
                document.getClassification(),
                relationId(document.getFolder()),
                relationId(document.getCategory()));
    }

    public static DocumentUpdatedEvent documentUpdated(Document document) {
        return new DocumentUpdatedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                document.getStatus(),
                relationId(document.getFolder()),
                relationId(document.getCategory()),
                relationId(document.getRetentionPolicy()));
    }

    public static DocumentDeletedEvent documentDeleted(Document document) {
        return new DocumentDeletedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion());
    }

    public static DocumentRestoredEvent documentRestored(Document document) {
        return new DocumentRestoredEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                document.getStatus());
    }

    public static DocumentArchivedEvent documentArchived(Document document) {
        return new DocumentArchivedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion());
    }

    public static DocumentMovedEvent documentMoved(Document document) {
        return new DocumentMovedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                relationId(document.getFolder()));
    }

    public static DocumentRenamedEvent documentRenamed(Document document) {
        return new DocumentRenamedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                document.getTitle());
    }

    public static DocumentClassificationChangedEvent documentClassificationChanged(
            Document document,
            DocumentClassification classification) {
        return new DocumentClassificationChangedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                classification);
    }

    public static DocumentActiveVersionChangedEvent documentActiveVersionChanged(Document document) {
        DocumentVersion activeVersion = document.getActiveVersion();
        return new DocumentActiveVersionChangedEvent(
                newEventId(),
                now(),
                document.getOrganizationId(),
                document.getId(),
                document.getOwnerId(),
                null,
                document.getVersion(),
                activeVersion != null ? activeVersion.getId() : null,
                activeVersion != null && activeVersion.getVersionNumber() != null
                        ? activeVersion.getVersionNumber().getValue()
                        : null);
    }

    public static DocumentVersionCreatedEvent documentVersionCreated(DocumentVersion version) {
        Document document = version.getDocument();
        return new DocumentVersionCreatedEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                version.getUploadedById(),
                null,
                version.getVersion(),
                version.getId(),
                version.getVersionNumber() != null ? version.getVersionNumber().getValue() : null,
                version.getMimeType(),
                version.getVersionStatus(),
                relationId(version.getStorageProvider()));
    }

    public static DocumentVersionActivatedEvent documentVersionActivated(DocumentVersion version) {
        Document document = version.getDocument();
        return new DocumentVersionActivatedEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                version.getUploadedById(),
                null,
                version.getVersion(),
                version.getId(),
                version.getVersionNumber() != null ? version.getVersionNumber().getValue() : null);
    }

    public static FolderCreatedEvent folderCreated(Folder folder) {
        return new FolderCreatedEvent(
                newEventId(),
                now(),
                folder.getOrganizationId(),
                null,
                folder.getOwnerId(),
                null,
                folder.getVersion(),
                folder.getId(),
                relationId(folder.getParentFolder()),
                folder.getName(),
                pathValue(folder),
                depthValue(folder));
    }

    public static FolderUpdatedEvent folderUpdated(Folder folder) {
        return new FolderUpdatedEvent(
                newEventId(),
                now(),
                folder.getOrganizationId(),
                null,
                folder.getOwnerId(),
                null,
                folder.getVersion(),
                folder.getId(),
                folder.getName(),
                pathValue(folder),
                depthValue(folder));
    }

    public static FolderMovedEvent folderMoved(Folder folder) {
        return new FolderMovedEvent(
                newEventId(),
                now(),
                folder.getOrganizationId(),
                null,
                folder.getOwnerId(),
                null,
                folder.getVersion(),
                folder.getId(),
                relationId(folder.getParentFolder()),
                pathValue(folder),
                depthValue(folder));
    }

    public static FolderDeletedEvent folderDeleted(Folder folder) {
        return new FolderDeletedEvent(
                newEventId(),
                now(),
                folder.getOrganizationId(),
                null,
                folder.getOwnerId(),
                null,
                folder.getVersion(),
                folder.getId());
    }

    public static FolderRestoredEvent folderRestored(Folder folder) {
        return new FolderRestoredEvent(
                newEventId(),
                now(),
                folder.getOrganizationId(),
                null,
                folder.getOwnerId(),
                null,
                folder.getVersion(),
                folder.getId());
    }

    public static DocumentCategoryCreatedEvent categoryCreated(DocumentCategory category) {
        return new DocumentCategoryCreatedEvent(
                newEventId(),
                now(),
                category.getOrganizationId(),
                null,
                null,
                null,
                category.getVersion(),
                category.getId(),
                category.getCode(),
                category.getName(),
                relationId(category.getParentCategory()));
    }

    public static DocumentCategoryUpdatedEvent categoryUpdated(DocumentCategory category) {
        return new DocumentCategoryUpdatedEvent(
                newEventId(),
                now(),
                category.getOrganizationId(),
                null,
                null,
                null,
                category.getVersion(),
                category.getId(),
                category.getName(),
                relationId(category.getParentCategory()),
                relationId(category.getDefaultRetentionPolicy()));
    }

    public static DocumentCategoryDeletedEvent categoryDeleted(DocumentCategory category) {
        return new DocumentCategoryDeletedEvent(
                newEventId(),
                now(),
                category.getOrganizationId(),
                null,
                null,
                null,
                category.getVersion(),
                category.getId());
    }

    public static DocumentCategoryRestoredEvent categoryRestored(DocumentCategory category) {
        return new DocumentCategoryRestoredEvent(
                newEventId(),
                now(),
                category.getOrganizationId(),
                null,
                null,
                null,
                category.getVersion(),
                category.getId());
    }

    public static DocumentMetadataCreatedEvent metadataCreated(DocumentMetadata metadata) {
        Document document = metadata.getDocument();
        return new DocumentMetadataCreatedEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                null,
                null,
                metadata.getVersion(),
                metadata.getId(),
                relationId(metadata.getDocumentVersion()));
    }

    public static DocumentMetadataUpdatedEvent metadataUpdated(DocumentMetadata metadata) {
        Document document = metadata.getDocument();
        return new DocumentMetadataUpdatedEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                null,
                null,
                metadata.getVersion(),
                metadata.getId(),
                relationId(metadata.getDocumentVersion()));
    }

    public static RetentionPolicyCreatedEvent retentionPolicyCreated(DocumentRetentionPolicy policy) {
        return new RetentionPolicyCreatedEvent(
                newEventId(),
                now(),
                policy.getOrganizationId(),
                null,
                null,
                null,
                policy.getVersion(),
                policy.getId(),
                policy.getName(),
                policy.getRetentionDays(),
                policy.getActionOnExpiry(),
                policy.getLegalHold());
    }

    public static RetentionPolicyUpdatedEvent retentionPolicyUpdated(DocumentRetentionPolicy policy) {
        return new RetentionPolicyUpdatedEvent(
                newEventId(),
                now(),
                policy.getOrganizationId(),
                null,
                null,
                null,
                policy.getVersion(),
                policy.getId(),
                policy.getName(),
                policy.getRetentionDays(),
                policy.getActionOnExpiry(),
                policy.getLegalHold());
    }

    public static RetentionPolicyDeletedEvent retentionPolicyDeleted(DocumentRetentionPolicy policy) {
        return new RetentionPolicyDeletedEvent(
                newEventId(),
                now(),
                policy.getOrganizationId(),
                null,
                null,
                null,
                policy.getVersion(),
                policy.getId());
    }

    public static RetentionPolicyRestoredEvent retentionPolicyRestored(DocumentRetentionPolicy policy) {
        return new RetentionPolicyRestoredEvent(
                newEventId(),
                now(),
                policy.getOrganizationId(),
                null,
                null,
                null,
                policy.getVersion(),
                policy.getId());
    }

    public static DocumentSharedEvent documentShared(DocumentShare share) {
        Document document = share.getDocument();
        return new DocumentSharedEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                share.getCreatedById(),
                null,
                share.getVersion(),
                share.getId(),
                share.getShareType(),
                share.getSharedWithUserId(),
                share.getSharedWithRoleId(),
                share.getPermission(),
                share.getExpiresAt());
    }

    public static DocumentShareExpiredEvent documentShareExpired(DocumentShare share) {
        Document document = share.getDocument();
        return new DocumentShareExpiredEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                share.getCreatedById(),
                null,
                share.getVersion(),
                share.getId(),
                share.getExpiresAt());
    }

    public static DocumentShareRevokedEvent documentShareRevoked(DocumentShare share) {
        Document document = share.getDocument();
        return new DocumentShareRevokedEvent(
                newEventId(),
                now(),
                document != null ? document.getOrganizationId() : null,
                document != null ? document.getId() : null,
                share.getCreatedById(),
                null,
                share.getVersion(),
                share.getId());
    }

    public static StorageProviderCreatedEvent storageProviderCreated(StorageProvider provider) {
        return new StorageProviderCreatedEvent(
                newEventId(),
                now(),
                null,
                null,
                null,
                null,
                provider.getVersion(),
                provider.getId(),
                provider.getProviderName(),
                provider.getProviderType(),
                provider.getIsDefault());
    }

    public static StorageProviderUpdatedEvent storageProviderUpdated(StorageProvider provider) {
        return new StorageProviderUpdatedEvent(
                newEventId(),
                now(),
                null,
                null,
                null,
                null,
                provider.getVersion(),
                provider.getId(),
                provider.getProviderName(),
                provider.getProviderType(),
                provider.getActive());
    }

    public static StorageProviderActivatedEvent storageProviderActivated(StorageProvider provider) {
        return new StorageProviderActivatedEvent(
                newEventId(),
                now(),
                null,
                null,
                null,
                null,
                provider.getVersion(),
                provider.getId(),
                provider.getProviderName());
    }

    public static StorageProviderDeactivatedEvent storageProviderDeactivated(StorageProvider provider) {
        return new StorageProviderDeactivatedEvent(
                newEventId(),
                now(),
                null,
                null,
                null,
                null,
                provider.getVersion(),
                provider.getId(),
                provider.getProviderName());
    }

    public static StorageProviderDefaultChangedEvent storageProviderDefaultChanged(StorageProvider provider) {
        return new StorageProviderDefaultChangedEvent(
                newEventId(),
                now(),
                null,
                null,
                null,
                null,
                provider.getVersion(),
                provider.getId(),
                provider.getProviderName());
    }

    private static UUID newEventId() {
        return UUID.randomUUID();
    }

    private static Instant now() {
        return Instant.now();
    }

    private static UUID relationId(Object entity) {
        if (entity instanceof Document document) {
            return document.getId();
        }
        if (entity instanceof DocumentVersion version) {
            return version.getId();
        }
        if (entity instanceof Folder folder) {
            return folder.getId();
        }
        if (entity instanceof DocumentCategory category) {
            return category.getId();
        }
        if (entity instanceof DocumentRetentionPolicy policy) {
            return policy.getId();
        }
        if (entity instanceof StorageProvider provider) {
            return provider.getId();
        }
        return null;
    }

    private static String pathValue(Folder folder) {
        return folder.getPathMetadata() != null ? folder.getPathMetadata().getMaterializedPath() : null;
    }

    private static Integer depthValue(Folder folder) {
        return folder.getPathMetadata() != null ? folder.getPathMetadata().getDepthLevel() : null;
    }
}
