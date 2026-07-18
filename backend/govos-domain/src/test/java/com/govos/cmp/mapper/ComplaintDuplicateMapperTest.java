package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateDto;
import com.govos.cmp.entity.ComplaintDuplicate;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintDuplicateMapperTest {

    private ComplaintDuplicateMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintDuplicateMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithPrimaryComplaintId() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicate entity = CmpTestFixtures.duplicateLink(UUID.randomUUID(), primaryId, duplicateId);

        ComplaintDuplicateDto dto = mapper.toDto(entity);

        assertThat(dto.primaryComplaintId()).isEqualTo(primaryId);
        assertThat(dto.duplicateComplaintId()).isEqualTo(duplicateId);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(primaryId, duplicateId);

        ComplaintDuplicate entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getDuplicateComplaintId()).isEqualTo(duplicateId);
    }
}
