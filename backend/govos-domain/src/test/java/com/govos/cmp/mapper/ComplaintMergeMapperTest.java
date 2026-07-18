package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;
import com.govos.cmp.entity.ComplaintMerge;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintMergeMapperTest {

    private ComplaintMergeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintMergeMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithSurvivingComplaintId() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMerge entity = CmpTestFixtures.mergeLink(UUID.randomUUID(), survivingId, mergedId);

        ComplaintMergeDto dto = mapper.toDto(entity);

        assertThat(dto.survivingComplaintId()).isEqualTo(survivingId);
        assertThat(dto.mergedComplaintId()).isEqualTo(mergedId);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(survivingId, mergedId);

        ComplaintMerge entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getMergedComplaintId()).isEqualTo(mergedId);
    }
}
