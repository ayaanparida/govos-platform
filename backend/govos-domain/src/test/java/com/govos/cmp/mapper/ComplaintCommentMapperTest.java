package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.entity.ComplaintComment;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintCommentMapperTest {

    private ComplaintCommentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintCommentMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithComplaintId() {
        UUID complaintId = UUID.randomUUID();
        ComplaintComment entity = CmpTestFixtures.comment(UUID.randomUUID(), complaintId);

        ComplaintCommentDto dto = mapper.toDto(entity);

        assertThat(dto.complaintId()).isEqualTo(complaintId);
        assertThat(dto.commentText()).isEqualTo(entity.getCommentText());
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = CmpTestFixtures.commentCreateRequest(complaintId);

        ComplaintComment entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getAuthorUserId()).isEqualTo(CmpTestFixtures.OFFICER_ID);
    }
}
