package com.govos.api.cmp.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchDocumentUpdateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import com.govos.srh.exception.SearchDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintSearchIntegrationV2Test {

    @Mock
    private SearchApplicationService searchApplicationService;

    private ComplaintSearchIntegration integration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INDEX_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SRH_DOCUMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @BeforeEach
    void setUp() {
        integration = new ComplaintSearchIntegrationImpl(
                searchApplicationService,
                new ObjectMapper().registerModule(new JavaTimeModule()),
                new ComplaintSearchMapper());
    }

    @Test
    void shouldCreateDocumentOnCreate() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        stubSearchIndex();

        integration.onCreated(complaint);

        SearchDocumentCreateRequest request = captureCreateRequest();
        assertThat(request.searchIndexId()).isEqualTo(INDEX_ID);
        assertThat(request.referenceId()).isEqualTo(COMPLAINT_ID);
        assertThat(request.searchDocumentId()).isEqualTo(COMPLAINT_ID);
        assertThat(request.organizationId()).isEqualTo(ORG_ID);
        assertThat(request.entityType()).isEqualTo(ComplaintSearchIntegrationImpl.ENTITY_TYPE);
        assertThat(request.documentJson()).contains("CMP-2026-0001");
        assertThat(request.active()).isTrue();
    }

    @Test
    void shouldUpdateDocumentOnLifecycleTransition() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED);
        stubSearchIndex();
        stubExistingDocument();

        integration.onSubmitted(complaint);

        ArgumentCaptor<SearchDocumentUpdateRequest> captor =
                ArgumentCaptor.forClass(SearchDocumentUpdateRequest.class);
        verify(searchApplicationService).updateDocument(eq(SRH_DOCUMENT_ID), captor.capture());
        assertThat(captor.getValue().referenceId()).isEqualTo(COMPLAINT_ID);
        assertThat(captor.getValue().organizationId()).isEqualTo(ORG_ID);
        assertThat(captor.getValue().version()).isEqualTo(2L);
    }

    @Test
    void shouldUpdateDocumentWithActiveFalseOnArchive() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.ARCHIVED);
        stubSearchIndex();
        stubExistingDocument();

        integration.onArchived(complaint);

        ArgumentCaptor<SearchDocumentUpdateRequest> captor =
                ArgumentCaptor.forClass(SearchDocumentUpdateRequest.class);
        verify(searchApplicationService).updateDocument(eq(SRH_DOCUMENT_ID), captor.capture());
        assertThat(captor.getValue().active()).isFalse();
        assertThat(captor.getValue().documentJson()).contains("\"active\":false");
    }

    @Test
    void shouldSoftDeleteExistingDocumentOnSoftDelete() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        stubSearchIndex();
        stubExistingDocument();

        integration.onSoftDeleted(complaint);

        verify(searchApplicationService).softDeleteDocument(SRH_DOCUMENT_ID);
        verify(searchApplicationService, never()).createDocument(any());
        verify(searchApplicationService, never()).updateDocument(any(), any());
    }

    @Test
    void shouldRestoreAndUpdateDocumentOnRestore() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        stubSearchIndex();
        when(searchApplicationService.listDocumentsByOrganization(ORG_ID)).thenReturn(List.of());
        when(searchApplicationService.restoreDocument(COMPLAINT_ID)).thenReturn(restoredDocument());

        integration.onRestored(complaint);

        verify(searchApplicationService).restoreDocument(COMPLAINT_ID);
        verify(searchApplicationService).updateDocument(eq(SRH_DOCUMENT_ID), any(SearchDocumentUpdateRequest.class));
    }

    @Test
    void shouldUpdateExistingDocumentOnRestoreWhenAlreadyActive() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        stubSearchIndex();
        stubExistingDocument();

        integration.onRestored(complaint);

        verify(searchApplicationService, never()).restoreDocument(any());
        verify(searchApplicationService).updateDocument(eq(SRH_DOCUMENT_ID), any(SearchDocumentUpdateRequest.class));
    }

    @Test
    void shouldCreateDocumentWhenUpdateTargetMissing() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.IN_PROGRESS);
        stubSearchIndex();
        when(searchApplicationService.listDocumentsByOrganization(ORG_ID)).thenReturn(List.of());

        integration.onInProgress(complaint);

        verify(searchApplicationService).createDocument(any(SearchDocumentCreateRequest.class));
        verify(searchApplicationService, never()).updateDocument(any(), any());
    }

    @Test
    void shouldReindexOnMergeDuplicateCommentAndAttachment() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.CLOSED);
        stubSearchIndex();
        stubExistingDocument();

        integration.onMergeCreated(complaint);
        integration.onDuplicateCreated(complaint);
        integration.onCommentAdded(complaint);
        integration.onAttachmentAdded(complaint);

        verify(searchApplicationService).getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE);
        verify(searchApplicationService, org.mockito.Mockito.times(4))
                .updateDocument(eq(SRH_DOCUMENT_ID), any(SearchDocumentUpdateRequest.class));
    }

    @Test
    void shouldBuildSearchTextFromComplaintFields() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED);
        stubSearchIndex();

        integration.onCreated(complaint);

        SearchDocumentCreateRequest request = captureCreateRequest();
        assertThat(request.searchText())
                .contains("CMP-2026-0001")
                .contains("Water leak")
                .contains("Description")
                .contains("WATER_SUPPLY")
                .contains("PIPE_LEAK")
                .contains("SUBMITTED")
                .contains("MEDIUM")
                .contains("CITIZEN_PORTAL")
                .contains("Main Street")
                .contains("Near temple")
                .contains("WARD-12")
                .contains("Village-A");
    }

    @Test
    void shouldWrapSearchFailureAsIntegrationException() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        stubSearchIndex();
        doThrow(new SearchDocumentException("index missing"))
                .when(searchApplicationService).createDocument(any(SearchDocumentCreateRequest.class));

        assertThatThrownBy(() -> integration.onCreated(complaint))
                .isInstanceOf(ComplaintSearchIntegrationException.class)
                .hasMessageContaining("Search integration failed");
    }

    @Test
    void shouldFailSoftDeleteWhenDocumentMissing() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        stubSearchIndex();
        when(searchApplicationService.listDocumentsByOrganization(ORG_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> integration.onSoftDeleted(complaint))
                .isInstanceOf(ComplaintSearchIntegrationException.class)
                .hasMessageContaining("Search document not found");
    }

    @Test
    void shouldIncludeOrganizationIdInEveryIndexedDocument() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.RESOLVED);
        stubSearchIndex();
        stubExistingDocument();

        integration.onResolved(complaint);

        ArgumentCaptor<SearchDocumentUpdateRequest> captor =
                ArgumentCaptor.forClass(SearchDocumentUpdateRequest.class);
        verify(searchApplicationService).updateDocument(eq(SRH_DOCUMENT_ID), captor.capture());
        assertThat(captor.getValue().organizationId()).isEqualTo(ORG_ID);
        assertThat(captor.getValue().metadataOrganizationId()).isEqualTo(ORG_ID);
        assertThat(captor.getValue().documentJson()).contains("\"organizationId\":\"" + ORG_ID + "\"");
    }

    private SearchDocumentCreateRequest captureCreateRequest() {
        ArgumentCaptor<SearchDocumentCreateRequest> captor =
                ArgumentCaptor.forClass(SearchDocumentCreateRequest.class);
        verify(searchApplicationService).createDocument(captor.capture());
        return captor.getValue();
    }

    private void stubSearchIndex() {
        when(searchApplicationService.getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE))
                .thenReturn(sampleIndexDto());
    }

    private void stubExistingDocument() {
        when(searchApplicationService.listDocumentsByOrganization(ORG_ID))
                .thenReturn(List.of(existingDocument()));
    }

    private static SearchIndexDto sampleIndexDto() {
        return new SearchIndexDto(
                INDEX_ID,
                ComplaintSearchIntegrationImpl.INDEX_CODE,
                "Complaint Search Index",
                "CMP complaint index",
                SearchEngineType.OPENSEARCH,
                SearchIndexStatus.ACTIVE,
                1,
                "cmp_complaint_v1",
                "cmp-complaint-read",
                0L,
                null,
                true,
                0L,
                "system",
                Instant.parse("2026-01-01T00:00:00Z"),
                "system",
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private static SearchDocumentDto existingDocument() {
        return new SearchDocumentDto(
                SRH_DOCUMENT_ID,
                "CMP-2026-0001",
                INDEX_ID,
                COMPLAINT_ID,
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                COMPLAINT_ID,
                "CMP-2026-0001",
                ORG_ID,
                "{\"complaintId\":\"" + COMPLAINT_ID + "\"}",
                "water leak",
                SearchDocumentStatus.INDEXED,
                3L,
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2026-01-02T01:00:00Z"),
                ORG_ID,
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                COMPLAINT_ID,
                "CMP-2026-0001",
                1,
                Instant.parse("2026-01-02T00:00:00Z"),
                Instant.parse("2026-01-02T01:00:00Z"),
                true,
                2L,
                "system",
                Instant.parse("2026-01-01T00:00:00Z"),
                "system",
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    private static SearchDocumentDto restoredDocument() {
        return existingDocument();
    }

    private static ComplaintDto sampleComplaint(ComplaintStatus status) {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                status, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), ORG_ID,
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, UUID.randomUUID(),
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, "WARD-12", "Village-A",
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", "Near temple", "560001", null,
                true, 3L, null, null, null, null);
    }
}
