package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintStatusHistoryDto;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintStatusHistoryMapperTest {

    private ComplaintStatusHistoryMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintStatusHistoryMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithComplaintId() {
        UUID complaintId = UUID.randomUUID();
        ComplaintStatusHistoryDto dto = mapper.toDto(CmpTestFixtures.statusHistory(complaintId));

        assertThat(dto.complaintId()).isEqualTo(complaintId);
        assertThat(dto.fromStatus()).isNotNull();
        assertThat(dto.toStatus()).isNotNull();
    }
}
