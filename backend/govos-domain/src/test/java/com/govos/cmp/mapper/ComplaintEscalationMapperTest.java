package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.entity.ComplaintEscalation;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintEscalationMapperTest {

    private ComplaintEscalationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintEscalationMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithComplaintId() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalation entity = CmpTestFixtures.escalation(UUID.randomUUID(), complaintId);

        ComplaintEscalationDto dto = mapper.toDto(entity);

        assertThat(dto.complaintId()).isEqualTo(complaintId);
        assertThat(dto.escalationLevel()).isEqualTo(entity.getEscalationLevel());
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = CmpTestFixtures.escalationCreateRequest(complaintId);

        ComplaintEscalation entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getEscalationReason()).isEqualTo(request.escalationReason());
    }
}
