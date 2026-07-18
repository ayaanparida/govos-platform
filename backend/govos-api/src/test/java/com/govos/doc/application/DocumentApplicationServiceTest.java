package com.govos.doc.application;

import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.DocumentListResponse;
import com.govos.doc.dto.document.DocumentResponse;
import com.govos.doc.dto.document.DocumentSummaryResponse;
import com.govos.doc.dto.document.UpdateDocumentRequest;
import com.govos.doc.entity.Document;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.mapper.DocumentMapper;
import com.govos.doc.service.DocumentService;
import com.govos.doc.validator.DocumentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentApplicationServiceTest {

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock private DocumentService documentService;
    @Mock private DocumentMapper documentMapper;
    @Mock private DocumentValidator documentValidator;

    private DocumentApplicationServiceImpl documentApplicationService;

    @BeforeEach
    void setUp() {
        documentApplicationService = new DocumentApplicationServiceImpl(
                documentService,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                documentMapper,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                documentValidator,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    @Test
    void shouldValidateMapAndReturnResponseWhenCreatingDocument() {
        CreateDocumentRequest request = new CreateDocumentRequest(
                "Title", "Desc", ORG_ID, OWNER_ID, DocumentClassification.INTERNAL,
                null, null, null, null, null, null, "DOC-001", null, "application/pdf", true);
        Document entity = new Document();
        entity.setId(DOCUMENT_ID);
        DocumentResponse response = new DocumentResponse(
                DOCUMENT_ID, null, "Title", "Desc", ORG_ID, OWNER_ID, DocumentStatus.UPLOADED,
                DocumentClassification.INTERNAL, "application/pdf", null, null, null, "DOC-001", null,
                null, null, null, null, true, 0L);

        when(documentService.createDocument(request)).thenReturn(entity);
        when(documentMapper.toResponse(entity)).thenReturn(response);

        DocumentResponse result = documentApplicationService.createDocument(request);

        assertThat(result).isSameAs(response);
        verify(documentValidator).validateCreate(request);
        verify(documentService).createDocument(request);
        verify(documentMapper).toResponse(entity);
    }

    @Test
    void shouldValidateAndDeleteDocument() {
        documentApplicationService.deleteDocument(DOCUMENT_ID);

        verify(documentValidator).validateDelete(DOCUMENT_ID);
        verify(documentService).deleteDocument(DOCUMENT_ID);
    }

    @Test
    void shouldMapDocumentPageToListResponse() {
        Document entity = new Document();
        entity.setId(DOCUMENT_ID);
        DocumentListResponse listResponse = new DocumentListResponse(
                List.of(new DocumentSummaryResponse(
                        DOCUMENT_ID, null, "Title", DocumentStatus.UPLOADED,
                        DocumentClassification.INTERNAL, null, "DOC-001", ORG_ID, null)),
                1L, 1, 0, 20);

        when(documentService.findByOrganization(eq(ORG_ID), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(documentMapper.toListResponse(any())).thenReturn(listResponse);

        DocumentListResponse result = documentApplicationService.listDocuments(ORG_ID, PageRequest.of(0, 20));

        assertThat(result).isSameAs(listResponse);
        verify(documentMapper).toListResponse(any());
    }

    @Test
    void shouldValidateUpdateBeforeDelegatingToDomainService() {
        UpdateDocumentRequest request = new UpdateDocumentRequest(
                "Updated", "Desc", null, null, null, null, null,
                null, null, null, null, null, null, true, 0L);
        Document entity = new Document();
        DocumentResponse response = new DocumentResponse(
                DOCUMENT_ID, null, "Updated", "Desc", ORG_ID, OWNER_ID, DocumentStatus.UPLOADED,
                DocumentClassification.INTERNAL, null, null, null, null, null, null,
                null, null, null, null, true, 0L);

        when(documentService.updateDocument(DOCUMENT_ID, request)).thenReturn(entity);
        when(documentMapper.toResponse(entity)).thenReturn(response);

        DocumentResponse result = documentApplicationService.updateDocument(DOCUMENT_ID, request);

        assertThat(result).isSameAs(response);
        verify(documentValidator).validateUpdate(request);
        verify(documentService).updateDocument(DOCUMENT_ID, request);
    }
}
