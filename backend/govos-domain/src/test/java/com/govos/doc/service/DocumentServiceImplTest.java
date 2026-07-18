package com.govos.doc.service.impl;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentCategory;
import com.govos.doc.entity.DocumentRetentionPolicy;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.Folder;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.FolderNotFoundException;
import com.govos.doc.mapper.DocumentMapper;
import com.govos.doc.repository.DocumentCategoryRepository;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentRetentionPolicyRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.repository.FolderRepository;
import com.govos.doc.service.DocumentVersionService;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.DocumentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock private DocumentRepository documentRepository;
    @Mock private FolderRepository folderRepository;
    @Mock private DocumentCategoryRepository categoryRepository;
    @Mock private DocumentRetentionPolicyRepository retentionPolicyRepository;
    @Mock private DocumentVersionRepository documentVersionRepository;
    @Mock private DocumentMapper documentMapper;
    @Mock private DocumentValidator documentValidator;
    @Mock private DocumentVersionService documentVersionService;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private DocumentServiceImpl service;

    private Document document;

    @BeforeEach
    void setUp() {
        document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
    }

    @Test
    void shouldCreateDocumentAndPublishEvent() {
        CreateDocumentRequest request = DocumentTestFixtures.createDocumentRequest();
        when(documentMapper.toEntity(request)).thenReturn(document);
        when(documentRepository.save(document)).thenReturn(document);
        when(documentRepository.findByOrganizationIdAndDocumentNumberAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "DOC-001")).thenReturn(Optional.empty());

        Document saved = service.createDocument(request);

        assertThat(saved).isSameAs(document);
        assertThat(saved.getDeleted()).isFalse();
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        verify(documentValidator).validateCreate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateDocumentNumberExists() {
        CreateDocumentRequest request = DocumentTestFixtures.createDocumentRequest();
        when(documentRepository.findByOrganizationIdAndDocumentNumberAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "DOC-001")).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> service.createDocument(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void shouldUpdateDocumentAndPublishEvent() {
        UpdateDocumentRequest request = DocumentTestFixtures.updateDocumentRequest();
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = service.updateDocument(DocumentTestFixtures.DOCUMENT_ID, request);

        assertThat(saved).isSameAs(document);
        verify(documentValidator).validateUpdate(request);
        verify(documentMapper).updateEntity(request, document);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        document.setVersion(1L);
        UpdateDocumentRequest request = DocumentTestFixtures.updateDocumentRequest();
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));

        assertThatThrownBy(() -> service.updateDocument(DocumentTestFixtures.DOCUMENT_ID, request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldDeleteDocumentAndPublishEvent() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        service.deleteDocument(DocumentTestFixtures.DOCUMENT_ID);

        assertThat(document.getDeleted()).isTrue();
        assertThat(document.getStatus()).isEqualTo(DocumentStatus.DELETED);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenDocumentNotFound() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(DocumentTestFixtures.DOCUMENT_ID))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldResolveFolderOnCreate() {
        CreateDocumentRequest base = DocumentTestFixtures.createDocumentRequest();
        CreateDocumentRequest request = new CreateDocumentRequest(
                base.title(), base.description(), base.organizationId(), base.ownerId(),
                base.classification(), DocumentTestFixtures.FOLDER_ID, null, null,
                base.moduleCode(), base.entityType(), base.referenceId(), null,
                base.tags(), base.mimeType(), base.active());
        Folder folder = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        when(documentMapper.toEntity(request)).thenReturn(document);
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(folder));
        when(documentRepository.save(document)).thenReturn(document);

        service.createDocument(request);

        assertThat(document.getFolder()).isSameAs(folder);
    }

    @Test
    void shouldThrowWhenFolderNotFoundOnCreate() {
        CreateDocumentRequest base = DocumentTestFixtures.createDocumentRequest();
        CreateDocumentRequest request = new CreateDocumentRequest(
                base.title(), base.description(), base.organizationId(), base.ownerId(),
                base.classification(), DocumentTestFixtures.FOLDER_ID, null, null,
                base.moduleCode(), base.entityType(), base.referenceId(), null,
                base.tags(), base.mimeType(), base.active());
        when(documentMapper.toEntity(request)).thenReturn(document);
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createDocument(request))
                .isInstanceOf(FolderNotFoundException.class);
    }

    @Test
    void shouldChangeClassificationAndPublishEvent() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = service.changeClassification(
                DocumentTestFixtures.DOCUMENT_ID, DocumentClassification.CONFIDENTIAL);

        assertThat(saved.getClassification()).isEqualTo(DocumentClassification.CONFIDENTIAL);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldFindByOrganization() {
        when(documentRepository.findByOrganizationIdAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(document)));

        assertThat(service.findByOrganization(DocumentTestFixtures.ORG_ID, PageRequest.of(0, 10)).getContent())
                .containsExactly(document);
    }

    @Test
    void shouldFailValidationBeforeSaveOnCreate() {
        CreateDocumentRequest request = DocumentTestFixtures.createDocumentRequest();
        doThrow(new DocumentValidationException("invalid")).when(documentValidator).validateCreate(request);

        assertThatThrownBy(() -> service.createDocument(request))
                .isInstanceOf(DocumentValidationException.class);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void shouldRestoreDocumentAndPublishEvent() {
        document.setDeleted(true);
        document.setStatus(DocumentStatus.DELETED);
        when(documentRepository.findById(DocumentTestFixtures.DOCUMENT_ID)).thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = service.restoreDocument(DocumentTestFixtures.DOCUMENT_ID);

        assertThat(saved.getDeleted()).isFalse();
        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldArchiveDocumentAndPublishEvent() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = service.archiveDocument(DocumentTestFixtures.DOCUMENT_ID);

        assertThat(saved.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldMoveDocumentAndPublishEvent() {
        Folder folder = DocumentTestFixtures.folder(DocumentTestFixtures.FOLDER_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(folderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.FOLDER_ID))
                .thenReturn(Optional.of(folder));
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = service.moveDocument(DocumentTestFixtures.DOCUMENT_ID, DocumentTestFixtures.FOLDER_ID);

        assertThat(saved.getFolder()).isSameAs(folder);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRenameDocumentAndPublishEvent() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        Document saved = service.renameDocument(DocumentTestFixtures.DOCUMENT_ID, "New Title");

        assertThat(saved.getTitle()).isEqualTo("New Title");
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldActivateVersionAndPublishEvent() {
        DocumentVersion version = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findByIdAndDeletedFalse(DocumentTestFixtures.VERSION_ID))
                .thenReturn(Optional.of(version));

        service.activateVersion(DocumentTestFixtures.DOCUMENT_ID, DocumentTestFixtures.VERSION_ID);

        verify(documentVersionService).activateVersion(DocumentTestFixtures.VERSION_ID);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldFindByDocumentNumber() {
        when(documentRepository.findByOrganizationIdAndDocumentNumberAndDeletedFalse(
                DocumentTestFixtures.ORG_ID, "DOC-001")).thenReturn(Optional.of(document));

        assertThat(service.findByDocumentNumber(DocumentTestFixtures.ORG_ID, "DOC-001")).isSameAs(document);
    }

    @Test
    void shouldUpdateDocumentStatusWhenProvided() {
        UpdateDocumentRequest request = new UpdateDocumentRequest(
                null, null, DocumentStatus.ARCHIVED, null, null, null, null,
                null, null, null, null, null, null, null, 0L);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentRepository.save(document)).thenReturn(document);

        service.updateDocument(DocumentTestFixtures.DOCUMENT_ID, request);

        assertThat(document.getStatus()).isEqualTo(DocumentStatus.ARCHIVED);
        verify(documentValidator).validateStatusTransition(DocumentStatus.UPLOADED, DocumentStatus.ARCHIVED);
    }

    @Test
    void shouldResolveCategoryAndRetentionPolicyOnCreate() {
        CreateDocumentRequest base = DocumentTestFixtures.createDocumentRequest();
        CreateDocumentRequest request = new CreateDocumentRequest(
                base.title(), base.description(), base.organizationId(), base.ownerId(),
                base.classification(), null, DocumentTestFixtures.CATEGORY_ID,
                DocumentTestFixtures.POLICY_ID, base.moduleCode(), base.entityType(),
                base.referenceId(), null, base.tags(), base.mimeType(), base.active());
        DocumentCategory category = DocumentTestFixtures.category(DocumentTestFixtures.CATEGORY_ID);
        DocumentRetentionPolicy policy = DocumentTestFixtures.retentionPolicy(DocumentTestFixtures.POLICY_ID);
        when(documentMapper.toEntity(request)).thenReturn(document);
        when(categoryRepository.findByIdAndDeletedFalse(DocumentTestFixtures.CATEGORY_ID))
                .thenReturn(Optional.of(category));
        when(retentionPolicyRepository.findByIdAndDeletedFalse(DocumentTestFixtures.POLICY_ID))
                .thenReturn(Optional.of(policy));
        when(documentRepository.save(document)).thenReturn(document);

        service.createDocument(request);

        assertThat(document.getCategory()).isSameAs(category);
        assertThat(document.getRetentionPolicy()).isSameAs(policy);
    }
}
