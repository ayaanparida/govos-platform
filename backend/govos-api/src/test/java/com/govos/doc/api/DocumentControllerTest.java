package com.govos.doc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.common.validation.PaginationRequest;
import com.govos.doc.api.advice.DocumentRestExceptionHandler;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.DocumentListResponse;
import com.govos.doc.dto.document.DocumentResponse;
import com.govos.doc.dto.document.DocumentSummaryResponse;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import com.govos.doc.exception.DocumentNotFoundException;
import com.govos.doc.exception.DocumentValidationException;
import com.govos.doc.exception.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {

    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OWNER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Mock private DocumentApplicationService documentApplicationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        DocumentController controller = new DocumentController(documentApplicationService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .setControllerAdvice(new DocumentRestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void shouldCreateDocument() throws Exception {
        CreateDocumentRequest request = sampleCreateRequest();
        when(documentApplicationService.createDocument(any(CreateDocumentRequest.class)))
                .thenReturn(sampleDocumentResponse());

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(DOCUMENT_ID.toString()));
    }

    @Test
    void shouldRejectCreateWhenValidationFails() throws Exception {
        when(documentApplicationService.createDocument(any(CreateDocumentRequest.class)))
                .thenThrow(new DocumentValidationException("title is required"));

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DOCUMENT_VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnDocumentById() throws Exception {
        when(documentApplicationService.findDocument(DOCUMENT_ID)).thenReturn(sampleDocumentResponse());

        mockMvc.perform(get("/api/v1/documents/{documentId}", DOCUMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Document"));
    }

    @Test
    void shouldReturnNotFoundWhenDocumentMissing() throws Exception {
        when(documentApplicationService.findDocument(DOCUMENT_ID))
                .thenThrow(new DocumentNotFoundException(DOCUMENT_ID));

        mockMvc.perform(get("/api/v1/documents/{documentId}", DOCUMENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldReturnConflictWhenDuplicateDocumentNumber() throws Exception {
        ValidationResult result = new ValidationResult();
        result.addError("documentNumber", "Duplicate", "DOC_DUPLICATE_DOCUMENT_NUMBER");
        when(documentApplicationService.createDocument(any(CreateDocumentRequest.class)))
                .thenThrow(new DocumentValidationException(result));

        mockMvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void shouldDeleteDocument() throws Exception {
        mockMvc.perform(delete("/api/v1/documents/{documentId}", DOCUMENT_ID))
                .andExpect(status().isNoContent());

        verify(documentApplicationService).deleteDocument(DOCUMENT_ID);
    }

    @Test
    void shouldListDocumentsWithPagination() throws Exception {
        DocumentListResponse listResponse = new DocumentListResponse(
                List.of(new DocumentSummaryResponse(
                        DOCUMENT_ID, null, "Test Document", DocumentStatus.UPLOADED,
                        DocumentClassification.INTERNAL, "application/pdf", "DOC-001", ORG_ID, null)),
                1L, 1, 0, 20);
        when(documentApplicationService.listDocuments(eq(ORG_ID), any(Pageable.class)))
                .thenReturn(listResponse);

        mockMvc.perform(get("/api/v1/documents")
                        .param("organizationId", ORG_ID.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.documents[0].id").value(DOCUMENT_ID.toString()));
    }

    private CreateDocumentRequest sampleCreateRequest() {
        return new CreateDocumentRequest(
                "Test Document", "Description", ORG_ID, OWNER_ID, DocumentClassification.INTERNAL,
                null, null, null, null, null, null, "DOC-001", null, "application/pdf", true);
    }

    private DocumentResponse sampleDocumentResponse() {
        return new DocumentResponse(
                DOCUMENT_ID, null, "Test Document", "Description", ORG_ID, OWNER_ID,
                DocumentStatus.UPLOADED, DocumentClassification.INTERNAL, "application/pdf",
                null, null, null, "DOC-001", null, null, null, null, null, true, 0L);
    }
}
