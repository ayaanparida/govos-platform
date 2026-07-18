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
import com.govos.doc.support.DocumentTestFixtures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentEventsTest {

    @Test
    void shouldBuildAllDocumentLifecycleEvents() {
        Document document = fullDocument();

        assertEvent(DocumentEvents.documentCreated(document), DocumentEventTypes.DOCUMENT_CREATED);
        assertEvent(DocumentEvents.documentUpdated(document), DocumentEventTypes.DOCUMENT_UPDATED);
        assertEvent(DocumentEvents.documentDeleted(document), DocumentEventTypes.DOCUMENT_DELETED);
        assertEvent(DocumentEvents.documentRestored(document), DocumentEventTypes.DOCUMENT_RESTORED);
        assertEvent(DocumentEvents.documentArchived(document), DocumentEventTypes.DOCUMENT_ARCHIVED);
        assertEvent(DocumentEvents.documentMoved(document), DocumentEventTypes.DOCUMENT_MOVED);
        assertEvent(DocumentEvents.documentRenamed(document), DocumentEventTypes.DOCUMENT_RENAMED);
        assertEvent(
                DocumentEvents.documentClassificationChanged(document, DocumentClassification.RESTRICTED),
                DocumentEventTypes.DOCUMENT_CLASSIFICATION_CHANGED);
        assertEvent(DocumentEvents.documentActiveVersionChanged(document), DocumentEventTypes.DOCUMENT_ACTIVE_VERSION_CHANGED);
    }

    @Test
    void shouldBuildVersionEvents() {
        Document document = fullDocument();
        DocumentVersion version = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);
        document.setActiveVersion(version);

        assertEvent(DocumentEvents.documentVersionCreated(version), DocumentEventTypes.DOCUMENT_VERSION_CREATED);
        assertEvent(DocumentEvents.documentVersionActivated(version), DocumentEventTypes.DOCUMENT_VERSION_ACTIVATED);
        assertThat(DocumentEvents.documentActiveVersionChanged(document).activeVersionId())
                .isEqualTo(DocumentTestFixtures.VERSION_ID);
    }

    @Test
    void shouldBuildFolderEvents() {
        Folder folder = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        Folder parent = DocumentTestFixtures.folder(DocumentTestFixtures.CATEGORY_ID);
        folder.setParentFolder(parent);

        assertEvent(DocumentEvents.folderCreated(folder), DocumentEventTypes.FOLDER_CREATED);
        assertEvent(DocumentEvents.folderUpdated(folder), DocumentEventTypes.FOLDER_UPDATED);
        assertEvent(DocumentEvents.folderMoved(folder), DocumentEventTypes.FOLDER_MOVED);
        assertEvent(DocumentEvents.folderDeleted(folder), DocumentEventTypes.FOLDER_DELETED);
        assertEvent(DocumentEvents.folderRestored(folder), DocumentEventTypes.FOLDER_RESTORED);
    }

    @Test
    void shouldBuildCategoryEvents() {
        DocumentCategory category = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);
        category.setParentCategory(DocumentTestFixtures.category(DocumentTestFixtures.FOLDER_ID));
        category.setDefaultRetentionPolicy(DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID));

        assertEvent(DocumentEvents.categoryCreated(category), DocumentEventTypes.DOCUMENT_CATEGORY_CREATED);
        assertEvent(DocumentEvents.categoryUpdated(category), DocumentEventTypes.DOCUMENT_CATEGORY_UPDATED);
        assertEvent(DocumentEvents.categoryDeleted(category), DocumentEventTypes.DOCUMENT_CATEGORY_DELETED);
        assertEvent(DocumentEvents.categoryRestored(category), DocumentEventTypes.DOCUMENT_CATEGORY_RESTORED);
    }

    @Test
    void shouldBuildMetadataEvents() {
        Document document = fullDocument();
        DocumentVersion version = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);
        DocumentMetadata metadata = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);
        metadata.setDocumentVersion(version);

        assertEvent(DocumentEvents.metadataCreated(metadata), DocumentEventTypes.DOCUMENT_METADATA_CREATED);
        assertEvent(DocumentEvents.metadataUpdated(metadata), DocumentEventTypes.DOCUMENT_METADATA_UPDATED);
    }

    @Test
    void shouldBuildRetentionPolicyEvents() {
        DocumentRetentionPolicy policy = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);

        assertEvent(DocumentEvents.retentionPolicyCreated(policy), DocumentEventTypes.RETENTION_POLICY_CREATED);
        assertEvent(DocumentEvents.retentionPolicyUpdated(policy), DocumentEventTypes.RETENTION_POLICY_UPDATED);
        assertEvent(DocumentEvents.retentionPolicyDeleted(policy), DocumentEventTypes.RETENTION_POLICY_DELETED);
        assertEvent(DocumentEvents.retentionPolicyRestored(policy), DocumentEventTypes.RETENTION_POLICY_RESTORED);
    }

    @Test
    void shouldBuildShareEvents() {
        Document document = fullDocument();
        DocumentShare share = DocumentTestFixtures.share(DocumentTestFixtures.SHARE_ID, document);

        assertEvent(DocumentEvents.documentShared(share), DocumentEventTypes.DOCUMENT_SHARED);
        assertEvent(DocumentEvents.documentShareExpired(share), DocumentEventTypes.DOCUMENT_SHARE_EXPIRED);
        assertEvent(DocumentEvents.documentShareRevoked(share), DocumentEventTypes.DOCUMENT_SHARE_REVOKED);
    }

    @Test
    void shouldBuildStorageProviderEvents() {
        StorageProvider provider = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);

        assertEvent(DocumentEvents.storageProviderCreated(provider), DocumentEventTypes.STORAGE_PROVIDER_CREATED);
        assertEvent(DocumentEvents.storageProviderUpdated(provider), DocumentEventTypes.STORAGE_PROVIDER_UPDATED);
        assertEvent(DocumentEvents.storageProviderActivated(provider), DocumentEventTypes.STORAGE_PROVIDER_ACTIVATED);
        assertEvent(DocumentEvents.storageProviderDeactivated(provider), DocumentEventTypes.STORAGE_PROVIDER_DEACTIVATED);
        assertEvent(DocumentEvents.storageProviderDefaultChanged(provider), DocumentEventTypes.STORAGE_PROVIDER_DEFAULT_CHANGED);
    }

    @Test
    void shouldHandleNullDocumentOnVersionCreatedEvent() {
        DocumentVersion version = DocumentTestFixtures.documentVersion(
                DocumentTestFixtures.VERSION_ID,
                DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID));
        version.setDocument(null);

        DocumentVersionCreatedEvent event = DocumentEvents.documentVersionCreated(version);

        assertThat(event.documentId()).isNull();
        assertThat(event.organizationId()).isNull();
    }

    private static Document fullDocument() {
        Document document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        document.setFolder(DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID));
        document.setCategory(DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID));
        document.setRetentionPolicy(DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID));
        return document;
    }

    private static void assertEvent(DocumentDomainEvent event, String expectedType) {
        assertThat(event.eventType()).isEqualTo(expectedType);
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }
}
