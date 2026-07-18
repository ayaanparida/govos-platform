package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.mapper.ComplaintAssignmentMapper;
import com.govos.cmp.repository.ComplaintAssignmentRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintAssignmentValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintAssignmentServiceImplTest {

    @Mock private ComplaintAssignmentRepository complaintAssignmentRepository;
    @Mock private ComplaintAssignmentMapper complaintAssignmentMapper;
    @Mock private ComplaintAssignmentValidator complaintAssignmentValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintAssignmentServiceImpl service;

    @Test
    void shouldCreateAssignment() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignmentCreateRequest request = CmpTestFixtures.assignmentCreateRequest(complaintId);
        ComplaintAssignment entity = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        ComplaintAssignmentDto dto = assignmentDto(entity);

        when(complaintAssignmentMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(complaintId))
                .thenReturn(CmpTestFixtures.complaint(complaintId, entity.getComplaint().getStatus()));
        when(complaintAssignmentRepository.save(entity)).thenReturn(entity);
        when(complaintAssignmentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.createAssignment(request)).isEqualTo(dto);
        verify(complaintAssignmentValidator).validateCreate(request);
        assertThat(entity.getDeleted()).isFalse();
    }

    @Test
    void shouldReturnCurrentAssignment() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignment entity = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        ComplaintAssignmentDto dto = assignmentDto(entity);

        when(complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(entity));
        when(complaintAssignmentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.getCurrentAssignment(complaintId)).contains(dto);
    }

    @Test
    void shouldListAssignments() {
        UUID complaintId = UUID.randomUUID();
        ComplaintAssignment entity = CmpTestFixtures.assignment(UUID.randomUUID(), complaintId);
        ComplaintAssignmentDto dto = assignmentDto(entity);

        when(complaintAssignmentRepository.findAllByComplaintIdAndDeletedFalse(complaintId))
                .thenReturn(List.of(entity));
        when(complaintAssignmentMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listAssignments(complaintId)).containsExactly(dto);
    }

    private static ComplaintAssignmentDto assignmentDto(ComplaintAssignment entity) {
        return new ComplaintAssignmentDto(
                entity.getId(), null, entity.getComplaint().getId(), entity.getAssignmentType(),
                entity.getDepartmentId(), entity.getOfficeId(), entity.getOfficerUserId(),
                entity.getAssignedByUserId(), entity.getAssignmentStatus(), entity.getAssignedAt(),
                entity.getAcceptedAt(), entity.getRejectedAt(), entity.getRejectionReasonKey(),
                entity.getRemarks(), entity.getIsCurrent(), true, 0L, null, null, null, null);
    }
}
