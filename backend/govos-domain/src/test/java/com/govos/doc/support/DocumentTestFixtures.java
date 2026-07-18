package com.govos.doc.support;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentAccessLog;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentMetadata;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.entity.DocumentShare;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.Folder;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.AccessOperation;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.enums.OcrStatus;
import com.govos.doc.enums.PreviewStatus;
import com.govos.doc.enums.RetentionAction;
import com.govos.doc.enums.ShareType;
import com.govos.doc.enums.StorageProviderType;
import com.govos.doc.enums.VirusScanStatus;
import com.govos.doc.valueobject.DocumentChecksum;
import com.govos.doc.valueobject.DocumentPath;
import com.govos.doc.valueobject.FileSize;
import com.govos.doc.valueobject.ShareToken;
import com.govos.doc.valueobject.StorageLocation;
import com.govos.doc.valueobject.VersionNumber;

import java.time.Instant;
import java.util.UUID;

public final class DocumentTestFixtures {

    public static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID VERSION_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID FOLDER_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final UUID CATEGORY_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    public static final UUID METADATA_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");
    public static final UUID POLICY_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    public static final UUID SHARE_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID PROVIDER_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    public static final String SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    public static final String STORAGE_KEY = "org/documents/file.pdf";

    private DocumentTestFixtures() {
    }

    public static Document document(UUID id) {
        Document entity = new Document();
        entity.setId(id);
        entity.setTitle("Test Document");
        entity.setDescription("Description");
        entity.setOrganizationId(ORG_ID);
        entity.setOwnerId(OWNER_ID);
        entity.setStatus(DocumentStatus.UPLOADED);
        entity.setClassification(DocumentClassification.INTERNAL);
        entity.setDocumentNumber("DOC-001");
        entity.setMimeType("application/pdf");
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static DocumentVersion documentVersion(UUID id, Document document) {
        DocumentVersion entity = new DocumentVersion();
        entity.setId(id);
        entity.setDocument(document);
        entity.setVersionNumber(new VersionNumber(1, "v1"));
        entity.setChecksum(new DocumentChecksum(SHA256));
        entity.setStorageLocation(new StorageLocation(STORAGE_KEY));
        entity.setFileSize(new FileSize(1024L));
        entity.setMimeType("application/pdf");
        entity.setOriginalFilename("file.pdf");
        entity.setUploadedById(USER_ID);
        entity.setUploadedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setStorageProvider(storageProvider(PROVIDER_ID));
        entity.setVersionStatus(DocumentVersionStatus.ACTIVE);
        entity.setVirusScanStatus(VirusScanStatus.PENDING);
        entity.setOcrStatus(OcrStatus.NOT_STARTED);
        entity.setPreviewStatus(PreviewStatus.NOT_GENERATED);
        entity.setImmutable(true);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static Folder folder(UUID id) {
        Folder entity = new Folder();
        entity.setId(id);
        entity.setName("Root");
        entity.setOrganizationId(ORG_ID);
        entity.setOwnerId(OWNER_ID);
        entity.setPathMetadata(new DocumentPath("/Root", 0));
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static DocumentCategory category(UUID id) {
        DocumentCategory entity = new DocumentCategory();
        entity.setId(id);
        entity.setCode("CAT-001");
        entity.setName("General");
        entity.setOrganizationId(ORG_ID);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static DocumentMetadata metadata(UUID id, Document document) {
        DocumentMetadata entity = new DocumentMetadata();
        entity.setId(id);
        entity.setDocument(document);
        entity.setOcrLanguage("en");
        entity.setPageCount(1);
        entity.setWatermarkApplied(false);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static DocumentRetentionPolicy retentionPolicy(UUID id) {
        DocumentRetentionPolicy entity = new DocumentRetentionPolicy();
        entity.setId(id);
        entity.setName("Default Policy");
        entity.setOrganizationId(ORG_ID);
        entity.setRetentionDays(365);
        entity.setActionOnExpiry(RetentionAction.ARCHIVE);
        entity.setLegalHold(false);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static DocumentShare share(UUID id, Document document) {
        DocumentShare entity = new DocumentShare();
        entity.setId(id);
        entity.setDocument(document);
        entity.setShareType(ShareType.USER);
        entity.setSharedWithUserId(USER_ID);
        entity.setCreatedById(OWNER_ID);
        entity.setPermission("READ");
        entity.setShareToken(new ShareToken());
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static DocumentAccessLog accessLog(UUID id, Document document) {
        DocumentAccessLog entity = new DocumentAccessLog();
        entity.setId(id);
        entity.setDocument(document);
        entity.setUserId(USER_ID);
        entity.setOperation(AccessOperation.DOWNLOAD);
        entity.setAccessedAt(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setSuccess(true);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static StorageProvider storageProvider(UUID id) {
        StorageProvider entity = new StorageProvider();
        entity.setId(id);
        entity.setProviderName("minio-primary");
        entity.setProviderType(StorageProviderType.MINIO);
        entity.setBucketName("govos-documents");
        entity.setEndpoint("http://localhost:9000");
        entity.setRegion("local");
        entity.setEncryptionEnabled(true);
        entity.setIsDefault(true);
        entity.setActive(true);
        entity.setDeleted(false);
        entity.setVersion(0L);
        return entity;
    }

    public static CreateDocumentRequest createDocumentRequest() {
        return new CreateDocumentRequest(
                "Test Document",
                "Description",
                ORG_ID,
                OWNER_ID,
                DocumentClassification.INTERNAL,
                null,
                null,
                null,
                "CMP",
                "Complaint",
                null,
                "DOC-001",
                "tag1,tag2",
                "application/pdf",
                true);
    }

    public static UpdateDocumentRequest updateDocumentRequest() {
        return new UpdateDocumentRequest(
                "Updated Title",
                "Updated",
                null,
                DocumentClassification.CONFIDENTIAL,
                null,
                null,
                null,
                "CMP",
                "Complaint",
                null,
                "DOC-002",
                null,
                "application/pdf",
                true,
                0L);
    }

    public static CreateDocumentVersionRequest createVersionRequest(UUID documentId) {
        return new CreateDocumentVersionRequest(
                documentId,
                1,
                "v1",
                SHA256,
                PROVIDER_ID,
                STORAGE_KEY,
                null,
                null,
                "application/pdf",
                "file.pdf",
                1024L,
                USER_ID,
                Instant.parse("2026-01-01T00:00:00Z"),
                VirusScanStatus.PENDING,
                OcrStatus.NOT_STARTED,
                PreviewStatus.NOT_GENERATED,
                DocumentVersionStatus.ACTIVE);
    }

    public static CreateFolderRequest createFolderRequest() {
        return new CreateFolderRequest(
                "Folder",
                ORG_ID,
                OWNER_ID,
                null,
                "/Folder",
                0,
                "FLD-001",
                true);
    }

    public static CreateDocumentCategoryRequest createCategoryRequest() {
        return new CreateDocumentCategoryRequest(
                "CAT-001",
                "General",
                ORG_ID,
                null,
                null,
                "application/pdf",
                "Category",
                true);
    }

    public static CreateRetentionPolicyRequest createRetentionPolicyRequest() {
        return new CreateRetentionPolicyRequest(
                "POL-001",
                "Default Policy",
                ORG_ID,
                365,
                RetentionAction.ARCHIVE,
                false,
                "Policy",
                true);
    }

    public static CreateShareRequest createShareRequest(UUID documentId) {
        return new CreateShareRequest(
                documentId,
                ShareType.USER,
                USER_ID,
                null,
                null,
                OWNER_ID,
                Instant.parse("2099-01-01T00:00:00Z"),
                "READ",
                null,
                null);
    }

    public static CreateStorageProviderRequest createStorageProviderRequest() {
        return new CreateStorageProviderRequest(
                "SP-001",
                "minio-primary",
                StorageProviderType.MINIO,
                "govos-documents",
                "http://localhost:9000",
                "local",
                true,
                false,
                "secret-ref",
                true);
    }

    public static UpdateDocumentMetadataRequest updateMetadataRequest() {
        return new UpdateDocumentMetadataRequest(
                "ocr text",
                "en",
                0.95,
                "{}",
                "{}",
                1,
                "en",
                false,
                true,
                0L);
    }
}
