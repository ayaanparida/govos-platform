package com.govos.api.cmp.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.srh.dto.IndexSearchDocumentRequest;
import com.govos.srh.exception.SearchIndexNotFoundException;
import com.govos.srh.service.SearchIndexService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ComplaintSearchIntegrationTest {

    @Mock private SearchIndexService searchIndexService;

    private ComplaintSearchIntegration integration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @BeforeEach
    void setUp() {
        integration = new ComplaintSearchIntegrationImpl(
                searchIndexService,
                new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void shouldIndexOnCreate() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);

        integration.onCreated(complaint);

        IndexSearchDocumentRequest request = captureIndexRequest();
        assertDocumentRequest(request, complaint, true);
    }

    @Test
    void shouldReindexOnUpdate() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED);

        integration.onUpdated(complaint);

        IndexSearchDocumentRequest request = captureReindexRequest();
        assertDocumentRequest(request, complaint, true);
    }

    @Test
    void shouldMarkArchivedOnArchive() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.ARCHIVED);

        integration.onArchived(complaint);

        IndexSearchDocumentRequest request = captureReindexRequest();
        assertThat(request.documentJson()).contains("\"active\":false");
    }

    @Test
    void shouldIndexOnRestore() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);

        integration.onRestored(complaint);

        verify(searchIndexService).index(any(IndexSearchDocumentRequest.class));
    }

    @Test
    void shouldReindexOnMerge() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.CLOSED);

        integration.onMergeCreated(complaint);

        IndexSearchDocumentRequest request = captureReindexRequest();
        assertThat(request.documentId()).isEqualTo(COMPLAINT_ID);
    }

    @Test
    void shouldReindexOnDuplicate() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.REJECTED);

        integration.onDuplicateCreated(complaint);

        verify(searchIndexService).reindex(any(IndexSearchDocumentRequest.class));
    }

    @Test
    void shouldRemoveOnSoftDelete() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);

        integration.onSoftDeleted(complaint);

        verify(searchIndexService).remove(ComplaintSearchIntegrationImpl.INDEX_CODE, COMPLAINT_ID);
        verifyNoMoreInteractions(searchIndexService);
    }

    @Test
    void shouldReindexOnReopen() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.IN_PROGRESS);

        integration.onReopened(complaint);

        verify(searchIndexService).reindex(any(IndexSearchDocumentRequest.class));
    }

    @Test
    void shouldBuildSearchTextFromComplaintFields() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED);

        integration.onCreated(complaint);

        IndexSearchDocumentRequest request = captureIndexRequest();
        assertThat(request.documentJson())
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
        doThrow(new SearchIndexNotFoundException("index missing"))
                .when(searchIndexService).index(any(IndexSearchDocumentRequest.class));

        assertThatThrownBy(() -> integration.onCreated(sampleComplaint(ComplaintStatus.DRAFT)))
                .isInstanceOf(ComplaintSearchIntegrationException.class)
                .hasMessageContaining("Search integration failed");
    }

    private IndexSearchDocumentRequest captureIndexRequest() {
        ArgumentCaptor<IndexSearchDocumentRequest> captor =
                ArgumentCaptor.forClass(IndexSearchDocumentRequest.class);
        verify(searchIndexService).index(captor.capture());
        return captor.getValue();
    }

    private IndexSearchDocumentRequest captureReindexRequest() {
        ArgumentCaptor<IndexSearchDocumentRequest> captor =
                ArgumentCaptor.forClass(IndexSearchDocumentRequest.class);
        verify(searchIndexService).reindex(captor.capture());
        return captor.getValue();
    }

    private void assertDocumentRequest(
            IndexSearchDocumentRequest request,
            ComplaintDto complaint,
            boolean active) {
        assertThat(request.indexCode()).isEqualTo(ComplaintSearchIntegrationImpl.INDEX_CODE);
        assertThat(request.documentId()).isEqualTo(complaint.id());
        assertThat(request.entityType()).isEqualTo(ComplaintSearchIntegrationImpl.ENTITY_TYPE);
        assertThat(request.documentJson()).contains(complaint.code());
        assertThat(request.documentJson()).contains("\"active\":" + active);
        assertThat(request.documentJson()).contains("\"deleted\":false");
    }

    private static ComplaintDto sampleComplaint(ComplaintStatus status) {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                status, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, UUID.randomUUID(),
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, "WARD-12", "Village-A",
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", "Near temple", "560001", null,
                true, 3L, null, null, null, null);
    }
}
