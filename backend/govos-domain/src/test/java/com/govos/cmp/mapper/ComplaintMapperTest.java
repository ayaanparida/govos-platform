package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.cmp.entity.Complaint;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintMapperTest {

    private ComplaintMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoIncludingEmbeddedLocation() {
        UUID id = UUID.randomUUID();
        Complaint entity = CmpTestFixtures.complaint(id, ComplaintStatus.DRAFT);
        entity.setCode("CMP-001");
        entity.setCreatedBy("system");
        entity.setCreatedDate(Instant.parse("2026-01-01T00:00:00Z"));

        ComplaintDto dto = mapper.toDto(entity);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.code()).isEqualTo("CMP-001");
        assertThat(dto.title()).isEqualTo(entity.getTitle());
        assertThat(dto.stateKey()).isEqualTo("KA");
        assertThat(dto.districtKey()).isEqualTo("BLR");
        assertThat(dto.latitude()).isEqualByComparingTo(entity.getLocation().getLatitude());
        assertThat(dto.createdBy()).isEqualTo("system");
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditAndLifecycleFields() {
        ComplaintCreateRequest request = CmpTestFixtures.createRequest();

        Complaint entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getCode()).isNull();
        assertThat(entity.getVersion()).isNull();
        assertThat(entity.getStatus()).isEqualTo(ComplaintStatus.DRAFT);
        assertThat(entity.getDeleted()).isFalse();
        assertThat(entity.getTitle()).isEqualTo(request.title());
        assertThat(entity.getOrganizationId()).isEqualTo(request.organizationId());
        assertThat(entity.getLocation().getStateKey()).isEqualTo("KA");
        assertThat(entity.getAssignments()).isNullOrEmpty();
    }

    @Test
    void shouldUpdateEntityFromRequestWithoutTouchingIgnoredFields() {
        Complaint entity = CmpTestFixtures.complaint(UUID.randomUUID(), ComplaintStatus.SUBMITTED);
        entity.setCode("CMP-001");
        entity.setCitizenUserId(CmpTestFixtures.CITIZEN_ID);
        entity.setSource(ComplaintSource.CITIZEN_PORTAL);
        entity.setCreatedBy("creator");
        entity.setCreatedDate(Instant.parse("2026-01-01T00:00:00Z"));
        entity.setAssignments(new ArrayList<>());

        ComplaintUpdateRequest request = new ComplaintUpdateRequest(
                "Updated", "Updated desc", ComplaintPriority.HIGH, "MOBILE",
                "NEW_CAT", "NEW_SUB", "TYPE", null, null,
                "TN", "CHN", null, null, null, null, null,
                "New address", null, "600001", null, false, 1L);

        mapper.updateEntity(request, entity);

        assertThat(entity.getCode()).isEqualTo("CMP-001");
        assertThat(entity.getStatus()).isEqualTo(ComplaintStatus.SUBMITTED);
        assertThat(entity.getCitizenUserId()).isEqualTo(CmpTestFixtures.CITIZEN_ID);
        assertThat(entity.getSource()).isEqualTo(ComplaintSource.CITIZEN_PORTAL);
        assertThat(entity.getCreatedBy()).isEqualTo("creator");
        assertThat(entity.getTitle()).isEqualTo("Updated");
        assertThat(entity.getPriority()).isEqualTo(ComplaintPriority.HIGH);
        assertThat(entity.getLocation().getStateKey()).isEqualTo("TN");
        assertThat(entity.getAssignments()).isEmpty();
    }
}
