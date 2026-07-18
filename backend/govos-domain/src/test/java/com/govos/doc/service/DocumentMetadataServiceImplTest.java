package com.govos.doc.service.impl;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentMetadata;
import com.govos.doc.event.publisher.DocumentEventPublisher;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.MetadataNotFoundException;
import com.govos.doc.mapper.DocumentMetadataMapper;
import com.govos.doc.repository.DocumentMetadataRepository;
import com.govos.doc.repository.DocumentRepository;
import com.govos.doc.repository.DocumentVersionRepository;
import com.govos.doc.support.DocumentTestFixtures;
import com.govos.doc.validator.DocumentMetadataValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentMetadataServiceImplTest {

    @Mock private DocumentMetadataRepository metadataRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private DocumentVersionRepository documentVersionRepository;
    @Mock private DocumentMetadataMapper metadataMapper;
    @Mock private DocumentMetadataValidator metadataValidator;
    @Mock private DocumentEventPublisher eventPublisher;

    @InjectMocks
    private DocumentMetadataServiceImpl service;

    private Document document;
    private DocumentMetadata metadata;

    @BeforeEach
    void setUp() {
        document = DocumentTestFixtures.document(DocumentTestFixtures.DOCUMENT_ID);
        metadata = DocumentTestFixtures.metadata(DocumentTestFixtures.METADATA_ID, document);
    }

    @Test
    void shouldCreateMetadataAndPublishEvent() {
        UpdateDocumentMetadataRequest request = DocumentTestFixtures.updateMetadataRequest();
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(metadataRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of());
        when(metadataRepository.save(any(DocumentMetadata.class))).thenAnswer(invocation -> {
            DocumentMetadata entity = invocation.getArgument(0);
            entity.setId(DocumentTestFixtures.METADATA_ID);
            return entity;
        });

        DocumentMetadata saved = service.createMetadata(DocumentTestFixtures.DOCUMENT_ID, null, request);

        assertThat(saved.getDocument()).isSameAs(document);
        assertThat(saved.getDeleted()).isFalse();
        verify(metadataValidator).validateDocumentScope(DocumentTestFixtures.DOCUMENT_ID, null);
        verify(metadataValidator).validateUpdate(request);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldRejectCreateWhenDuplicateMetadataScopeExists() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.of(document));
        when(metadataRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(metadata));

        assertThatThrownBy(() -> service.createMetadata(
                DocumentTestFixtures.DOCUMENT_ID, null, DocumentTestFixtures.updateMetadataRequest()))
                .isInstanceOf(DocumentValidationException.class);
        verify(metadataRepository, never()).save(any());
    }

    @Test
    void shouldRejectCreateWhenDocumentNotFound() {
        when(documentRepository.findByIdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createMetadata(
                DocumentTestFixtures.DOCUMENT_ID, null, DocumentTestFixtures.updateMetadataRequest()))
                .isInstanceOf(DocumentNotFoundException.class);
    }

    @Test
    void shouldUpdateMetadataAndPublishEvent() {
        UpdateDocumentMetadataRequest request = DocumentTestFixtures.updateMetadataRequest();
        when(metadataRepository.findByIdAndDeletedFalse(DocumentTestFixtures.METADATA_ID))
                .thenReturn(Optional.of(metadata));
        when(metadataRepository.save(metadata)).thenReturn(metadata);

        DocumentMetadata saved = service.updateMetadata(DocumentTestFixtures.METADATA_ID, request);

        assertThat(saved).isSameAs(metadata);
        verify(metadataValidator).validateUpdate(request);
        verify(metadataMapper).updateEntity(request, metadata);
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldReplaceMetadataAndPublishEvent() {
        UpdateDocumentMetadataRequest request = DocumentTestFixtures.updateMetadataRequest();
        when(metadataRepository.findByIdAndDeletedFalse(DocumentTestFixtures.METADATA_ID))
                .thenReturn(Optional.of(metadata));
        when(metadataRepository.save(metadata)).thenReturn(metadata);

        DocumentMetadata saved = service.replaceMetadata(DocumentTestFixtures.METADATA_ID, request);

        assertThat(saved.getOcrText()).isEqualTo("ocr text");
        verify(eventPublisher).publish(any());
    }

    @Test
    void shouldFindMetadataByDocumentScope() {
        when(metadataRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of(metadata));

        DocumentMetadata found = service.findMetadata(DocumentTestFixtures.DOCUMENT_ID, null);

        assertThat(found).isSameAs(metadata);
    }

    @Test
    void shouldThrowWhenMetadataNotFound() {
        when(metadataRepository.findByDocument_IdAndDeletedFalse(DocumentTestFixtures.DOCUMENT_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.findMetadata(DocumentTestFixtures.DOCUMENT_ID, null))
                .isInstanceOf(MetadataNotFoundException.class);
    }
}
