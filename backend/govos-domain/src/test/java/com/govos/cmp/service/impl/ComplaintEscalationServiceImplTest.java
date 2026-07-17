package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.entity.ComplaintEscalation;
import com.govos.cmp.mapper.ComplaintEscalationMapper;
import com.govos.cmp.repository.ComplaintEscalationRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintEscalationValidator;
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
class ComplaintEscalationServiceImplTest {

    @Mock private ComplaintEscalationRepository complaintEscalationRepository;
    @Mock private ComplaintEscalationMapper complaintEscalationMapper;
    @Mock private ComplaintEscalationValidator complaintEscalationValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintEscalationServiceImpl service;

    @Test
    void shouldCreateEscalation() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalationCreateRequest request = CmpTestFixtures.escalationCreateRequest(complaintId);
        ComplaintEscalation entity = CmpTestFixtures.escalation(UUID.randomUUID(), complaintId);
        ComplaintEscalationDto dto = escalationDto(entity);

        when(complaintEscalationMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(complaintId))
                .thenReturn(CmpTestFixtures.complaint(complaintId, entity.getComplaint().getStatus()));
        when(complaintEscalationRepository.save(entity)).thenReturn(entity);
        when(complaintEscalationMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.createEscalation(request)).isEqualTo(dto);
        verify(complaintEscalationValidator).validateCreate(request);
    }

    @Test
    void shouldListEscalations() {
        UUID complaintId = UUID.randomUUID();
        ComplaintEscalation entity = CmpTestFixtures.escalation(UUID.randomUUID(), complaintId);
        ComplaintEscalationDto dto = escalationDto(entity);

        when(complaintEscalationRepository.findAllByComplaintIdAndDeletedFalseOrderByEscalatedAtAsc(complaintId))
                .thenReturn(List.of(entity));
        when(complaintEscalationMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listEscalations(complaintId)).containsExactly(dto);
    }

    private static ComplaintEscalationDto escalationDto(ComplaintEscalation entity) {
        return new ComplaintEscalationDto(
                entity.getId(), null, entity.getComplaint().getId(),
                entity.getEscalationLevel(), entity.getEscalationReason(),
                null, null, null, null, entity.getEscalatedAt(),
                true, 0L, null, null, null, null);
    }
}
