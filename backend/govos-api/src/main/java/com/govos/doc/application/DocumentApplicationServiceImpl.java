package com.govos.doc.application;

import com.govos.doc.application.integration.DocumentAuditIntegration;
import com.govos.doc.application.integration.DocumentNotificationIntegration;
import com.govos.doc.application.integration.DocumentOcrIntegration;
import com.govos.doc.application.integration.DocumentPreviewIntegration;
import com.govos.doc.application.integration.DocumentSearchIntegration;
import com.govos.doc.application.integration.DocumentStorageIntegration;
import com.govos.doc.application.integration.DocumentWorkflowIntegration;
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
import com.govos.doc.mapper.DocumentCategoryMapper;
import com.govos.doc.mapper.DocumentMapper;
import com.govos.doc.mapper.DocumentMetadataMapper;
import com.govos.doc.mapper.DocumentRetentionPolicyMapper;
import com.govos.doc.mapper.DocumentShareMapper;
import com.govos.doc.mapper.DocumentVersionMapper;
import com.govos.doc.mapper.FolderMapper;
import com.govos.doc.mapper.StorageProviderMapper;
import com.govos.doc.service.DocumentCategoryService;
import com.govos.doc.service.DocumentMetadataService;
import com.govos.doc.service.DocumentRetentionPolicyService;
import com.govos.doc.service.DocumentService;
import com.govos.doc.service.DocumentShareService;
import com.govos.doc.service.DocumentVersionService;
import com.govos.doc.service.FolderService;
import com.govos.doc.service.StorageProviderService;
import com.govos.doc.validator.DocumentCategoryValidator;
import com.govos.doc.validator.DocumentMetadataValidator;
import com.govos.doc.validator.DocumentRetentionPolicyValidator;
import com.govos.doc.validator.DocumentShareValidator;
import com.govos.doc.validator.DocumentValidator;
import com.govos.doc.validator.DocumentVersionValidator;
import com.govos.doc.validator.FolderValidator;
import com.govos.doc.validator.StorageProviderValidator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentApplicationServiceImpl implements DocumentApplicationService {

    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;
    private final FolderService folderService;
    private final DocumentCategoryService documentCategoryService;
    private final DocumentMetadataService documentMetadataService;
    private final DocumentRetentionPolicyService documentRetentionPolicyService;
    private final DocumentShareService documentShareService;
    private final StorageProviderService storageProviderService;
    private final DocumentMapper documentMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final FolderMapper folderMapper;
    private final DocumentCategoryMapper documentCategoryMapper;
    private final DocumentMetadataMapper documentMetadataMapper;
    private final DocumentRetentionPolicyMapper documentRetentionPolicyMapper;
    private final DocumentShareMapper documentShareMapper;
    private final StorageProviderMapper storageProviderMapper;
    private final DocumentValidator documentValidator;
    private final DocumentVersionValidator documentVersionValidator;
    private final FolderValidator folderValidator;
    private final DocumentCategoryValidator documentCategoryValidator;
    private final DocumentMetadataValidator documentMetadataValidator;
    private final DocumentRetentionPolicyValidator documentRetentionPolicyValidator;
    private final DocumentShareValidator documentShareValidator;
    private final StorageProviderValidator storageProviderValidator;
    @SuppressWarnings("unused")
    private final Optional<DocumentSearchIntegration> documentSearchIntegration;
    @SuppressWarnings("unused")
    private final Optional<DocumentWorkflowIntegration> documentWorkflowIntegration;
    @SuppressWarnings("unused")
    private final Optional<DocumentNotificationIntegration> documentNotificationIntegration;
    @SuppressWarnings("unused")
    private final Optional<DocumentAuditIntegration> documentAuditIntegration;
    @SuppressWarnings("unused")
    private final Optional<DocumentStorageIntegration> documentStorageIntegration;
    @SuppressWarnings("unused")
    private final Optional<DocumentOcrIntegration> documentOcrIntegration;
    @SuppressWarnings("unused")
    private final Optional<DocumentPreviewIntegration> documentPreviewIntegration;

    public DocumentApplicationServiceImpl(
            DocumentService documentService,
            DocumentVersionService documentVersionService,
            FolderService folderService,
            DocumentCategoryService documentCategoryService,
            DocumentMetadataService documentMetadataService,
            DocumentRetentionPolicyService documentRetentionPolicyService,
            DocumentShareService documentShareService,
            StorageProviderService storageProviderService,
            DocumentMapper documentMapper,
            DocumentVersionMapper documentVersionMapper,
            FolderMapper folderMapper,
            DocumentCategoryMapper documentCategoryMapper,
            DocumentMetadataMapper documentMetadataMapper,
            DocumentRetentionPolicyMapper documentRetentionPolicyMapper,
            DocumentShareMapper documentShareMapper,
            StorageProviderMapper storageProviderMapper,
            DocumentValidator documentValidator,
            DocumentVersionValidator documentVersionValidator,
            FolderValidator folderValidator,
            DocumentCategoryValidator documentCategoryValidator,
            DocumentMetadataValidator documentMetadataValidator,
            DocumentRetentionPolicyValidator documentRetentionPolicyValidator,
            DocumentShareValidator documentShareValidator,
            StorageProviderValidator storageProviderValidator,
            Optional<DocumentSearchIntegration> documentSearchIntegration,
            Optional<DocumentWorkflowIntegration> documentWorkflowIntegration,
            Optional<DocumentNotificationIntegration> documentNotificationIntegration,
            Optional<DocumentAuditIntegration> documentAuditIntegration,
            Optional<DocumentStorageIntegration> documentStorageIntegration,
            Optional<DocumentOcrIntegration> documentOcrIntegration,
            Optional<DocumentPreviewIntegration> documentPreviewIntegration) {
        this.documentService = documentService;
        this.documentVersionService = documentVersionService;
        this.folderService = folderService;
        this.documentCategoryService = documentCategoryService;
        this.documentMetadataService = documentMetadataService;
        this.documentRetentionPolicyService = documentRetentionPolicyService;
        this.documentShareService = documentShareService;
        this.storageProviderService = storageProviderService;
        this.documentMapper = documentMapper;
        this.documentVersionMapper = documentVersionMapper;
        this.folderMapper = folderMapper;
        this.documentCategoryMapper = documentCategoryMapper;
        this.documentMetadataMapper = documentMetadataMapper;
        this.documentRetentionPolicyMapper = documentRetentionPolicyMapper;
        this.documentShareMapper = documentShareMapper;
        this.storageProviderMapper = storageProviderMapper;
        this.documentValidator = documentValidator;
        this.documentVersionValidator = documentVersionValidator;
        this.folderValidator = folderValidator;
        this.documentCategoryValidator = documentCategoryValidator;
        this.documentMetadataValidator = documentMetadataValidator;
        this.documentRetentionPolicyValidator = documentRetentionPolicyValidator;
        this.documentShareValidator = documentShareValidator;
        this.storageProviderValidator = storageProviderValidator;
        this.documentSearchIntegration = documentSearchIntegration;
        this.documentWorkflowIntegration = documentWorkflowIntegration;
        this.documentNotificationIntegration = documentNotificationIntegration;
        this.documentAuditIntegration = documentAuditIntegration;
        this.documentStorageIntegration = documentStorageIntegration;
        this.documentOcrIntegration = documentOcrIntegration;
        this.documentPreviewIntegration = documentPreviewIntegration;
    }

    @Override
    @Transactional
    public DocumentResponse createDocument(CreateDocumentRequest request) {
        documentValidator.validateCreate(request);
        return documentMapper.toResponse(documentService.createDocument(request));
    }

    @Override
    @Transactional
    public DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request) {
        documentValidator.validateUpdate(request);
        return documentMapper.toResponse(documentService.updateDocument(id, request));
    }

    @Override
    @Transactional
    public void deleteDocument(UUID id) {
        documentValidator.validateDelete(id);
        documentService.deleteDocument(id);
    }

    @Override
    @Transactional
    public DocumentResponse restoreDocument(UUID id) {
        return documentMapper.toResponse(documentService.restoreDocument(id));
    }

    @Override
    @Transactional
    public DocumentResponse archiveDocument(UUID id) {
        return documentMapper.toResponse(documentService.archiveDocument(id));
    }

    @Override
    @Transactional
    public DocumentResponse renameDocument(UUID id, String title) {
        return documentMapper.toResponse(documentService.renameDocument(id, title));
    }

    @Override
    @Transactional
    public DocumentResponse moveDocument(UUID id, UUID folderId) {
        return documentMapper.toResponse(documentService.moveDocument(id, folderId));
    }

    @Override
    @Transactional
    public DocumentResponse changeClassification(UUID id, DocumentClassification classification) {
        return documentMapper.toResponse(documentService.changeClassification(id, classification));
    }

    @Override
    @Transactional
    public DocumentResponse activateVersion(UUID documentId, UUID versionId) {
        return documentMapper.toResponse(documentService.activateVersion(documentId, versionId));
    }

    @Override
    public DocumentResponse findDocument(UUID id) {
        return documentMapper.toResponse(documentService.findById(id));
    }

    @Override
    public DocumentResponse findDocumentByNumber(UUID organizationId, String documentNumber) {
        return documentMapper.toResponse(documentService.findByDocumentNumber(organizationId, documentNumber));
    }

    @Override
    public DocumentListResponse listDocuments(UUID organizationId, Pageable pageable) {
        return documentMapper.toListResponse(documentService.findByOrganization(organizationId, pageable));
    }

    @Override
    public List<DocumentSearchResponse> searchDocuments(UUID organizationId, String query, Pageable pageable) {
        return documentService.findByOrganization(organizationId, pageable).stream()
                .map(documentMapper::toSearchResponse)
                .toList();
    }

    @Override
    @Transactional
    public DocumentVersionResponse createVersion(CreateDocumentVersionRequest request) {
        documentVersionValidator.validateCreate(request);
        return documentVersionMapper.toResponse(documentVersionService.createVersion(request));
    }

    @Override
    @Transactional
    public DocumentVersionResponse activateVersion(UUID versionId) {
        return documentVersionMapper.toResponse(documentVersionService.activateVersion(versionId));
    }

    @Override
    public DocumentVersionResponse findVersion(UUID versionId) {
        return documentVersionMapper.toResponse(documentVersionService.findVersion(versionId));
    }

    @Override
    public List<DocumentVersionSummaryResponse> listVersions(UUID documentId) {
        return documentVersionMapper.toSummaryResponseList(documentVersionService.listVersions(documentId));
    }

    @Override
    @Transactional
    public FolderResponse createFolder(CreateFolderRequest request) {
        folderValidator.validateCreate(request);
        return folderMapper.toResponse(folderService.createFolder(request));
    }

    @Override
    @Transactional
    public FolderResponse updateFolder(UUID id, UpdateFolderRequest request) {
        folderValidator.validateUpdate(request, id);
        var folder = folderService.findFolder(id);
        Long version = request.version() != null ? request.version() : folder.getVersion();
        if (request.name() != null) {
            folder = folderService.renameFolder(id, request.name(), version);
            version = folder.getVersion();
        }
        if (request.parentFolderId() != null) {
            folder = folderService.moveFolder(id, request.parentFolderId(), version);
        }
        return folderMapper.toResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse moveFolder(UUID id, UUID parentFolderId, Long version) {
        return folderMapper.toResponse(folderService.moveFolder(id, parentFolderId, version));
    }

    @Override
    @Transactional
    public void deleteFolder(UUID id) {
        folderValidator.validateDelete(id);
        folderService.deleteFolder(id);
    }

    @Override
    @Transactional
    public FolderResponse restoreFolder(UUID id) {
        return folderMapper.toResponse(folderService.restoreFolder(id));
    }

    @Override
    public FolderResponse findFolder(UUID id) {
        return folderMapper.toResponse(folderService.findFolder(id));
    }

    @Override
    @Transactional
    public DocumentCategoryResponse createCategory(CreateDocumentCategoryRequest request) {
        documentCategoryValidator.validateCreate(request);
        return documentCategoryMapper.toResponse(documentCategoryService.createCategory(request));
    }

    @Override
    @Transactional
    public DocumentCategoryResponse updateCategory(UUID id, UpdateDocumentCategoryRequest request) {
        documentCategoryValidator.validateUpdate(request, id);
        return documentCategoryMapper.toResponse(documentCategoryService.updateCategory(id, request));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        documentCategoryService.deleteCategory(id);
    }

    @Override
    @Transactional
    public DocumentCategoryResponse restoreCategory(UUID id) {
        return documentCategoryMapper.toResponse(documentCategoryService.restoreCategory(id));
    }

    @Override
    @Transactional
    public DocumentMetadataResponse createMetadata(
            UUID documentId, UUID documentVersionId, UpdateDocumentMetadataRequest request) {
        documentMetadataValidator.validateDocumentScope(documentId, documentVersionId);
        if (request != null) {
            documentMetadataValidator.validateUpdate(request);
        }
        return documentMetadataMapper.toResponse(
                documentMetadataService.createMetadata(documentId, documentVersionId, request));
    }

    @Override
    @Transactional
    public DocumentMetadataResponse updateMetadata(UUID id, UpdateDocumentMetadataRequest request) {
        documentMetadataValidator.validateUpdate(request);
        return documentMetadataMapper.toResponse(documentMetadataService.updateMetadata(id, request));
    }

    @Override
    @Transactional
    public DocumentMetadataResponse replaceMetadata(UUID id, UpdateDocumentMetadataRequest request) {
        documentMetadataValidator.validateUpdate(request);
        return documentMetadataMapper.toResponse(documentMetadataService.replaceMetadata(id, request));
    }

    @Override
    public DocumentMetadataResponse findMetadata(UUID documentId, UUID documentVersionId) {
        return documentMetadataMapper.toResponse(
                documentMetadataService.findMetadata(documentId, documentVersionId));
    }

    @Override
    @Transactional
    public RetentionPolicyResponse createRetentionPolicy(CreateRetentionPolicyRequest request) {
        documentRetentionPolicyValidator.validateCreate(request);
        return documentRetentionPolicyMapper.toResponse(documentRetentionPolicyService.createPolicy(request));
    }

    @Override
    @Transactional
    public RetentionPolicyResponse updateRetentionPolicy(UUID id, UpdateRetentionPolicyRequest request) {
        documentRetentionPolicyValidator.validateUpdate(request);
        return documentRetentionPolicyMapper.toResponse(documentRetentionPolicyService.updatePolicy(id, request));
    }

    @Override
    @Transactional
    public void deleteRetentionPolicy(UUID id) {
        documentRetentionPolicyService.deletePolicy(id);
    }

    @Override
    @Transactional
    public RetentionPolicyResponse restoreRetentionPolicy(UUID id) {
        return documentRetentionPolicyMapper.toResponse(documentRetentionPolicyService.restorePolicy(id));
    }

    @Override
    public RetentionPolicyResponse findRetentionPolicy(UUID id) {
        return documentRetentionPolicyMapper.toResponse(documentRetentionPolicyService.findPolicy(id));
    }

    @Override
    @Transactional
    public ShareResponse createShare(CreateShareRequest request) {
        documentShareValidator.validateCreate(request);
        return documentShareMapper.toResponse(documentShareService.createShare(request));
    }

    @Override
    @Transactional
    public ShareResponse expireShare(UUID id) {
        return documentShareMapper.toResponse(documentShareService.expireShare(id));
    }

    @Override
    @Transactional
    public ShareResponse revokeShare(UUID id) {
        return documentShareMapper.toResponse(documentShareService.revokeShare(id));
    }

    @Override
    public ShareResponse findShare(UUID id) {
        return documentShareMapper.toResponse(documentShareService.findShare(id));
    }

    @Override
    @Transactional
    public StorageProviderResponse createStorageProvider(CreateStorageProviderRequest request) {
        storageProviderValidator.validateCreate(request);
        return storageProviderMapper.toResponse(storageProviderService.createProvider(request));
    }

    @Override
    @Transactional
    public StorageProviderResponse updateStorageProvider(UUID id, UpdateStorageProviderRequest request) {
        storageProviderValidator.validateUpdate(request);
        return storageProviderMapper.toResponse(storageProviderService.updateProvider(id, request));
    }

    @Override
    @Transactional
    public StorageProviderResponse activateStorageProvider(UUID id) {
        return storageProviderMapper.toResponse(storageProviderService.activateProvider(id));
    }

    @Override
    @Transactional
    public StorageProviderResponse deactivateStorageProvider(UUID id) {
        return storageProviderMapper.toResponse(storageProviderService.deactivateProvider(id));
    }

    @Override
    @Transactional
    public StorageProviderResponse setDefaultStorageProvider(UUID id) {
        return storageProviderMapper.toResponse(storageProviderService.setDefaultProvider(id));
    }

    @Override
    public StorageProviderResponse findStorageProvider(UUID id) {
        return storageProviderMapper.toResponse(storageProviderService.findProvider(id));
    }
}
