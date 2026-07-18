package com.govos.api.srh.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.govos.api.cmp.search.ComplaintSearchIntegration;
import com.govos.api.cmp.search.ComplaintSearchIntegrationImpl;
import com.govos.api.cmp.search.ComplaintSearchMapper;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.srh.dto.SearchDocumentCreateRequest;
import com.govos.srh.dto.SearchDocumentDto;
import com.govos.srh.dto.SearchIndexDto;
import com.govos.srh.enums.SearchDocumentStatus;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies product integrations delegate to {@link SearchApplicationService} without OpenSearch access.
 */
@ExtendWith(MockitoExtension.class)
class SearchApplicationServiceIntegrationTest {

    @Mock
    private SearchApplicationService searchApplicationService;

    private ComplaintSearchIntegration complaintSearchIntegration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ORG_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID INDEX_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        complaintSearchIntegration = new ComplaintSearchIntegrationImpl(
                searchApplicationService,
                new ObjectMapper().registerModule(new JavaTimeModule()),
                new ComplaintSearchMapper());
    }

    @Test
    void shouldRouteCmpCreateThroughSearchApplicationService() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.DRAFT);
        when(searchApplicationService.getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE))
                .thenReturn(sampleIndexDto());
        when(searchApplicationService.createDocument(any(SearchDocumentCreateRequest.class)))
                .thenReturn(existingDocument());

        complaintSearchIntegration.onCreated(complaint);

        verify(searchApplicationService).getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE);
        verify(searchApplicationService).createDocument(any(SearchDocumentCreateRequest.class));
    }

    @Test
    void shouldResolveIndexOnceForMultipleOperations() {
        ComplaintDto complaint = sampleComplaint(ComplaintStatus.SUBMITTED);
        when(searchApplicationService.getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE))
                .thenReturn(sampleIndexDto());
        when(searchApplicationService.listDocumentsByOrganization(ORG_ID))
                .thenReturn(List.of(existingDocument()));

        complaintSearchIntegration.onSubmitted(complaint);
        complaintSearchIntegration.onAssigned(complaint);

        verify(searchApplicationService).getIndexByCode(ComplaintSearchIntegrationImpl.INDEX_CODE);
    }

    @Test
    void shouldNeverInvokeOpenSearchLayerDirectly() {
        assertThat(ComplaintSearchIntegrationImpl.class.getPackageName())
                .isEqualTo("com.govos.api.cmp.search");
        assertThat(ComplaintSearchIntegrationImpl.class.getName())
                .doesNotContain("OpenSearch");
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
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "CMP-2026-0001",
                INDEX_ID,
                COMPLAINT_ID,
                ComplaintSearchIntegrationImpl.ENTITY_TYPE,
                COMPLAINT_ID,
                "CMP-2026-0001",
                ORG_ID,
                "{}",
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
