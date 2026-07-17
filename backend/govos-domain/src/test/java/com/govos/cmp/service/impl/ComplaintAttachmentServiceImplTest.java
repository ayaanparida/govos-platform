package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentDto;
import com.govos.cmp.entity.ComplaintAttachment;
import com.govos.cmp.mapper.ComplaintAttachmentMapper;
import com.govos.cmp.repository.ComplaintAttachmentRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintAttachmentValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintAttachmentServiceImplTest {

    @Mock private ComplaintAttachmentRepository complaintAttachmentRepository;
    @Mock private ComplaintAttachmentMapper complaintAttachmentMapper;
    @Mock private ComplaintAttachmentValidator complaintAttachmentValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintAttachmentServiceImpl service;

    @Test
    void shouldAddAttachment() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachmentCreateRequest request = CmpTestFixtures.attachmentCreateRequest(complaintId);
        ComplaintAttachment entity = CmpTestFixtures.attachment(UUID.randomUUID(), complaintId);
        ComplaintAttachmentDto dto = new ComplaintAttachmentDto(
                entity.getId(), null, complaintId, entity.getDocumentId(), null,
                entity.getAttachmentType(), "Site photo", CmpTestFixtures.OFFICER_ID, 1,
                true, 0L, null, null, null, null);

        when(complaintAttachmentMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(complaintId))
                .thenReturn(CmpTestFixtures.complaint(complaintId, entity.getComplaint().getStatus()));
        when(complaintAttachmentRepository.save(entity)).thenReturn(entity);
        when(complaintAttachmentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.addAttachment(request)).isEqualTo(dto);
        verify(complaintAttachmentValidator).validateCreate(request);
    }

    @Test
    void shouldListAttachments() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAttachment entity = CmpTestFixtures.attachment(UUID.randomUUID(), complaintId);
        ComplaintAttachmentDto dto = new ComplaintAttachmentDto(
                entity.getId(), null, complaintId, entity.getDocumentId(), null,
                entity.getAttachmentType(), null, CmpTestFixtures.OFFICER_ID, null,
                true, 0L, null, null, null, null);

        when(complaintAttachmentRepository.findAllByComplaintIdAndDeletedFalse(complaintId))
                .thenReturn(List.of(entity));
        when(complaintAttachmentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listAttachments(complaintId)).containsExactly(dto);
    }
}
