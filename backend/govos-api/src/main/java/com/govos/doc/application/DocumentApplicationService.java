package com.govos.doc.application;

import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.DocumentCategoryResponse;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.DocumentListResponse;
import com.govos.doc.dto.document.DocumentResponse;
import com.govos.doc.dto.document.DocumentSearchResponse;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.FolderResponse;
import com.govos.doc.dto.folder.UpdateFolderRequest;
import com.govos.doc.dto.metadata.DocumentMetadataResponse;
import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.RetentionPolicyResponse;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.dto.share.ShareResponse;
import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.StorageProviderResponse;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.dto.version.DocumentVersionResponse;
import com.govos.doc.dto.version.DocumentVersionSummaryResponse;
import com.govos.doc.enums.DocumentClassification;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DocumentApplicationService {

    DocumentResponse createDocument(CreateDocumentRequest request);

    DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request);

    void deleteDocument(UUID id);

    DocumentResponse restoreDocument(UUID id);

    DocumentResponse archiveDocument(UUID id);

    DocumentResponse renameDocument(UUID id, String title);

    DocumentResponse moveDocument(UUID id, UUID folderId);

    DocumentResponse changeClassification(UUID id, DocumentClassification classification);

    DocumentResponse activateVersion(UUID documentId, UUID versionId);

    DocumentResponse findDocument(UUID id);

    DocumentResponse findDocumentByNumber(UUID organizationId, String documentNumber);

    DocumentListResponse listDocuments(UUID organizationId, Pageable pageable);

    List<DocumentSearchResponse> searchDocuments(UUID organizationId, String query, Pageable pageable);

    DocumentVersionResponse createVersion(CreateDocumentVersionRequest request);

    DocumentVersionResponse activateVersion(UUID versionId);

    DocumentVersionResponse findVersion(UUID versionId);

    List<DocumentVersionSummaryResponse> listVersions(UUID documentId);

    FolderResponse createFolder(CreateFolderRequest request);

    FolderResponse updateFolder(UUID id, UpdateFolderRequest request);

    FolderResponse moveFolder(UUID id, UUID parentFolderId, Long version);

    void deleteFolder(UUID id);

    FolderResponse restoreFolder(UUID id);

    FolderResponse findFolder(UUID id);

    DocumentCategoryResponse createCategory(CreateDocumentCategoryRequest request);

    DocumentCategoryResponse updateCategory(UUID id, UpdateDocumentCategoryRequest request);

    void deleteCategory(UUID id);

    DocumentCategoryResponse restoreCategory(UUID id);

    DocumentMetadataResponse createMetadata(
            UUID documentId, UUID documentVersionId, UpdateDocumentMetadataRequest request);

    DocumentMetadataResponse updateMetadata(UUID id, UpdateDocumentMetadataRequest request);

    DocumentMetadataResponse replaceMetadata(UUID id, UpdateDocumentMetadataRequest request);

    DocumentMetadataResponse findMetadata(UUID documentId, UUID documentVersionId);

    RetentionPolicyResponse createRetentionPolicy(CreateRetentionPolicyRequest request);

    RetentionPolicyResponse updateRetentionPolicy(UUID id, UpdateRetentionPolicyRequest request);

    void deleteRetentionPolicy(UUID id);

    RetentionPolicyResponse restoreRetentionPolicy(UUID id);

    RetentionPolicyResponse findRetentionPolicy(UUID id);

    ShareResponse createShare(CreateShareRequest request);

    ShareResponse expireShare(UUID id);

    ShareResponse revokeShare(UUID id);

    ShareResponse findShare(UUID id);

    StorageProviderResponse createStorageProvider(CreateStorageProviderRequest request);

    StorageProviderResponse updateStorageProvider(UUID id, UpdateStorageProviderRequest request);

    StorageProviderResponse activateStorageProvider(UUID id);

    StorageProviderResponse deactivateStorageProvider(UUID id);

    StorageProviderResponse setDefaultStorageProvider(UUID id);

    StorageProviderResponse findStorageProvider(UUID id);
}
