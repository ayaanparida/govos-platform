package com.govos.cmp.mapper;

import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentDto;
import com.govos.cmp.entity.ComplaintAttachment;
import com.govos.cmp.support.CmpTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ComplaintAttachmentMapperTest {

    private ComplaintAttachmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplaintAttachmentMapperImpl();
    }

    @Test
    void shouldMapEntityToDtoWithComplaintAndDocumentIds() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachment entity = CmpTestFixtures.attachment(UUID.randomUUID(), complaintId);

        ComplaintAttachmentDto dto = mapper.toDto(entity);

        assertThat(dto.complaintId()).isEqualTo(complaintId);
        assertThat(dto.documentId()).isEqualTo(CmpTestFixtures.DOCUMENT_ID);
    }

    @Test
    void shouldMapCreateRequestToEntityIgnoringAuditFields() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = CmpTestFixtures.attachmentCreateRequest(complaintId);

        ComplaintAttachment entity = mapper.toEntity(request);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getComplaint()).isNull();
        assertThat(entity.getDocumentId()).isEqualTo(CmpTestFixtures.DOCUMENT_ID);
    }
}
