package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.entity.ComplaintComment;
import com.govos.cmp.mapper.ComplaintCommentMapper;
import com.govos.cmp.repository.ComplaintCommentRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintCommentValidator;
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
class ComplaintCommentServiceImplTest {

    @Mock private ComplaintCommentRepository complaintCommentRepository;
    @Mock private ComplaintCommentMapper complaintCommentMapper;
    @Mock private ComplaintCommentValidator complaintCommentValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintCommentServiceImpl service;

    @Test
    void shouldAddComment() {
        UUID complaintId = UUID.randomUUID();
        ComplaintCommentCreateRequest request = CmpTestFixtures.commentCreateRequest(complaintId);
        ComplaintComment entity = CmpTestFixtures.comment(UUID.randomUUID(), complaintId);
        ComplaintCommentDto dto = new ComplaintCommentDto(
                entity.getId(), null, complaintId, entity.getAuthorUserId(), entity.getCommentText(),
                entity.getVisibility(), entity.getCommentType(), true, 0L, null, null, null, null);

        when(complaintCommentMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(complaintId)).thenReturn(CmpTestFixtures.complaint(complaintId, entity.getComplaint().getStatus()));
        when(complaintCommentRepository.save(entity)).thenReturn(entity);
        when(complaintCommentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.addComment(request)).isEqualTo(dto);
        verify(complaintCommentValidator).validateCreate(request);
    }

    @Test
    void shouldListComments() {
        UUID complaintId = UUID.randomUUID();
        ComplaintComment entity = CmpTestFixtures.comment(UUID.randomUUID(), complaintId);
        ComplaintCommentDto dto = new ComplaintCommentDto(
                entity.getId(), null, complaintId, entity.getAuthorUserId(), entity.getCommentText(),
                entity.getVisibility(), entity.getCommentType(), true, 0L, null, null, null, null);

        when(complaintCommentRepository.findAllByComplaintIdAndDeletedFalseOrderByCreatedDateAsc(complaintId))
                .thenReturn(List.of(entity));
        when(complaintCommentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listComments(complaintId)).containsExactly(dto);
    }
}
