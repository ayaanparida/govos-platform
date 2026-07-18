package com.govos.api.cmp.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.srh.application.SearchApplicationService;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintSearchIntegrationTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    private ComplaintSearchIntegration integration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INDEX_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        integration = new ComplaintSearchIntegrationImpl(
                searchApplicationService,
                new ObjectMapper().registerModule(new JavaTimeModule()),
                new ComplaintSearchMapper());
    }

    @Test
    void shouldAcceptLifecycleHooksWhenSearchApplicationServiceIsMocked() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED);
        when(searchApplicationService.getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE))
                .thenReturn(sampleIndexDto());
        when(searchApplicationService.createDocument(any(SearchDocumentCreateRequest.class)))
                .thenReturn(null);
        when(searchApplicationService.listDocumentsByOrganization(any(UUID.class)))
                .thenReturn(java.util.List.of());

        assertThatCode(() -> {
            integration.onCreated(complaint);
            integration.onUpdated(complaint);
            integration.onSubmitted(complaint);
            integration.onAssigned(complaint);
            integration.onInProgress(complaint);
            integration.onResolved(complaint);
            integration.onClosed(complaint);
            integration.onArchived(complaint);
            integration.onReopened(complaint);
            integration.onCommentAdded(complaint);
            integration.onAttachmentAdded(complaint);
            integration.onDuplicateCreated(complaint);
            integration.onMergeCreated(complaint);
        }).doesNotThrowAnyException();
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
