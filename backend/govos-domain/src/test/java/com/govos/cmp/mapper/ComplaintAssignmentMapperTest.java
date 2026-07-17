package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;
import com.govos.cmp.dto.ComplaintAssignmentUpdateRequest;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.enums.ComplaintAssignmentStatus;
import com.govos.cmp.enums.ComplaintAssignmentType;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintAssignmentMapperTest {

    private ComplaintAssignmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintAssignmentMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithComplaintId() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignment entity = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        entity.setAssignedByUserId(CmpTestFixtures.OFFICER_ID);
        entity.setAssignmentStatus(ComplaintAssignmentStatus.PENDING);
        entity.setAssignedAt(Instant.parse("2026-01-02T00:00:00Z"));

        ComplaintAssignmentDto dto = mapper.toDto(entity);

        assertThat(dto.complaintId()).isEqualTo(complaintId);
        assertThat(dto.officerUserId()).isEqualTo(CmpTestFixtures.OFFICER_ID);
        assertThat(dto.assignmentType()).isEqualTo(ComplaintAssignmentType.INITIAL);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(complaintId);

        ComplaintAssignment entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getAssignmentStatus()).isEqualTo(ComplaintAssignmentStatus.PENDING);
        assertThat(entity.getOfficerUserId()).isEqualTo(CmpTestFixtures.OFFICER_ID);
    }

    @Test
    void shouldUpdateEntityWithoutTouchingIgnoredFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignment entity = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        entity.setAssignedByUserId(CmpTestFixtures.OFFICER_ID);
        entity.setAssignedAt(Instant.parse("2026-01-01T00:00:00Z"));

        ComplaintAssignmentUpdateRequest request = new ComplaintAssignmentUpdateRequest(
                ComplaintAssignmentType.REASSIGNMENT, null, null, UUID.randomUUID(),
                ComplaintAssignmentStatus.ACCEPTED, null, "updated", false, true, 1L);

        mapper.updateEntity(request, entity);

        assertThat(entity.getAssignedByUserId()).isEqualTo(CmpTestFixtures.OFFICER_ID);
        assertThat(entity.getAssignedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(entity.getAssignmentType()).isEqualTo(ComplaintAssignmentType.REASSIGNMENT);
        assertThat(entity.getIsCurrent()).isFalse();
    }
}
