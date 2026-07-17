package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.entity.ComplaintFeedback;
import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintFeedbackMapperTest {

    private ComplaintFeedbackMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintFeedbackMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithComplaintId() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedback entity = CmpTestFixtures.feedback(UUID.randomUUID(), complaintId);

        ComplaintFeedbackDto dto = mapper.toDto(entity);

        assertThat(dto.complaintId()).isEqualTo(complaintId);
        assertThat(dto.rating()).isEqualTo(ComplaintFeedbackRating.FOUR);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = CmpTestFixtures.feedbackCreateRequest(complaintId);

        ComplaintFeedback entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getRatedByUserId()).isEqualTo(CmpTestFixtures.CITIZEN_ID);
    }

    @Test
    void shouldUpdateEntityWithoutTouchingIgnoredFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedback entity = CmpTestFixtures.feedback(UUID.randomUUID(), complaintId);
        entity.setRatedByUserId(CmpTestFixtures.CITIZEN_ID);
        entity.setRatedAt(Instant.parse("2026-01-01T00:00:00Z"));

        ComplaintFeedbackUpdateRequest request = new ComplaintFeedbackUpdateRequest(
                ComplaintFeedbackRating.FIVE, "Great", false, 1L);

        mapper.updateEntity(request, entity);

        assertThat(entity.getRatedByUserId()).isEqualTo(CmpTestFixtures.CITIZEN_ID);
        assertThat(entity.getRatedAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
        assertThat(entity.getRating()).isEqualTo(ComplaintFeedbackRating.FIVE);
    }
}
