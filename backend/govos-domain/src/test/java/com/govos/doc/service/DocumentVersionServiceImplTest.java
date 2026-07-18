package com.govos.doc.service.impl;

import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.entity.StorageProvider;
import com.govos.doc.enums.DocumentVersionStatus;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.StorageProviderNotFoundException;
import com.govos.doc.exception.VersionNotFoundException;
import com.govos.doc.mapper.DocumentVersionMapper;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.repository.StorageProviderRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.DocumentVersionValidator;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentVersionServiceImplTest {

    @Mock private DocumentVersionRepository documentVersionRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private StorageProviderRepository storageProviderRepository;
    @Mock private DocumentVersionMapper documentVersionMapper;
    @Mock private DocumentVersionValidator documentVersionValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private DocumentVersionServiceImpl service;

    private Document document;
    private DocumentVersion version;
    private StorageProvider provider;

    @BeforeEach
    void setUp() {
        document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        version = DocumentTestFixtures.documentVersion(DocumentTestFixtures.VERSION_ID, document);
        provider = DocumentTestFixtures.storageProvider(DocumentTestFixtures.PROVIDER_ID);
    }

    @Test
    void shouldCreateFirstVersionAndPublishEvents() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(
                DocumentTestFixtures.DOCUMENT_ID)).thenReturn(Optional.empty());
        when(documentVersionRepository.findByStorageLocation_StorageObjectKeyAndDeletedFalse(
                DocumentTestFixtures.STORAGE_KEY)).thenReturn(Optional.empty());
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));
        when(documentVersionMapper.toEntity(request)).thenReturn(version);
        when(documentVersionRepository.save(version)).thenReturn(version);
        when(documentRepository.save(document)).thenReturn(document);
        when(documentVersionRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(version));

        DocumentVersion saved = service.createVersion(request);

        assertThat(saved).isSameAs(version);
        assertThat(document.getActiveVersion()).isSameAs(version);
        verify(documentVersionValidator).validateCreate(request);
        verify(eventPublisher, org.mockito.Mockito.atLeastOnce()).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDocumentNotFound() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createVersion(request))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldRejectCreateWhenVersionSequenceInvalid() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(
                DocumentTestFixtures.DOCUMENT_ID)).thenReturn(Optional.of(version));

        assertThatThrownBy(() -> service.createVersion(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenStorageKeyDuplicate() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(
                DocumentTestFixtures.DOCUMENT_ID)).thenReturn(Optional.empty());
        when(documentVersionRepository.findByStorageLocation_StorageObjectKeyAndDeletedFalse(
                DocumentTestFixtures.STORAGE_KEY)).thenReturn(Optional.of(version));

        assertThatThrownBy(() -> service.createVersion(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenStorageProviderInactive() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        provider.setActive(false);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(
                DocumentTestFixtures.DOCUMENT_ID)).thenReturn(Optional.empty());
        when(documentVersionRepository.findByStorageLocation_StorageObjectKeyAndDeletedFalse(
                DocumentTestFixtures.STORAGE_KEY)).thenReturn(Optional.empty());
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.of(provider));

        assertThatThrownBy(() -> service.createVersion(request))
                .isInstanceOf(DocumentValidationException.class);
    }

    @Test
    void shouldRejectCreateWhenStorageProviderNotFound() {
        CreateDocumentVersionRequest request = DocumentTestFixtures.createVersionRequest(DocumentTestFixtures.DOCUMENT_ID);
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(documentVersionRepository.findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(
                DocumentTestFixtures.DOCUMENT_ID)).thenReturn(Optional.empty());
        when(documentVersionRepository.findByStorageLocation_StorageObjectKeyAndDeletedFalse(
                DocumentTestFixtures.STORAGE_KEY)).thenReturn(Optional.empty());
        when(storageProviderRepository.findByIdAndDeletedFalse(DocumentTestFixtures.PROVIDER_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createVersion(request))
                .isInstanceOf(StorageProviderNotFoundException.class);
    }

    @Test
    void shouldActivateVersionAndPublishEvent() {
        when(documentVersionRepository.findByIdAndDeletedFalse(DocumentTestFixtures.VERSION_ID))
                .thenReturn(Optional.of(version));
        when(documentVersionRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(version));
        when(documentRepository.save(document)).thenReturn(document);
        when(documentVersionRepository.save(version)).thenReturn(version);

        DocumentVersion saved = service.activateVersion(DocumentTestFixtures.VERSION_ID);

        assertThat(saved.getVersionStatus()).isEqualTo(DocumentVersionStatus.ACTIVE);
        assertThat(document.getActiveVersion()).isSameAs(version);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldThrowWhenVersionNotFound() {
        when(documentVersionRepository.findByIdAndDeletedFalse(DocumentTestFixtures.VERSION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findVersion(DocumentTestFixtures.VERSION_ID))
                .isInstanceOf(VersionNotFoundException.class);
    }

    @Test
    void shouldListVersionsWithPagination() {
        when(documentVersionRepository.findByDocument_IdAndDeletedFalse(
                DocumentTestFixtures.DOCUMENT_ID, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(version)));

        assertThat(service.listVersions(DocumentTestFixtures.DOCUMENT_ID, PageRequest.of(0, 10)).getContent())
                .containsExactly(version);
    }
}
