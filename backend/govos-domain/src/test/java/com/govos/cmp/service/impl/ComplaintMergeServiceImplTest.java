package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;
import com.govos.cmp.entity.ComplaintMerge;
import com.govos.cmp.mapper.ComplaintMergeMapper;
import com.govos.cmp.repository.ComplaintMergeRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintMergeValidator;
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
class ComplaintMergeServiceImplTest {

    @Mock private ComplaintMergeRepository complaintMergeRepository;
    @Mock private ComplaintMergeMapper complaintMergeMapper;
    @Mock private ComplaintMergeValidator complaintMergeValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintMergeServiceImpl service;

    @Test
    void shouldCreateMergeLink() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMergeCreateRequest request = CmpTestFixtures.mergeCreateRequest(survivingId, mergedId);
        ComplaintMerge entity = CmpTestFixtures.mergeLink(UUID.randomUUID(), survivingId, mergedId);
        ComplaintMergeDto dto = mergeDto(entity);

        when(complaintMergeMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(survivingId))
                .thenReturn(CmpTestFixtures.complaint(survivingId, entity.getComplaint().getStatus()));
        when(complaintMergeRepository.save(entity)).thenReturn(entity);
        when(complaintMergeMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.createMerge(request)).isEqualTo(dto);
        verify(complaintMergeValidator).validateCreate(request);
    }

    @Test
    void shouldListMerges() {
        UUID survivingId = UUID.randomUUID();
        UUID mergedId = UUID.randomUUID();
        ComplaintMerge entity = CmpTestFixtures.mergeLink(UUID.randomUUID(), survivingId, mergedId);
        ComplaintMergeDto dto = mergeDto(entity);

        when(complaintMergeRepository.findAllBySurvivingComplaintIdAndDeletedFalse(survivingId))
                .thenReturn(List.of(entity));
        when(complaintMergeMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listMerges(survivingId)).containsExactly(dto);
    }

    private static ComplaintMergeDto mergeDto(ComplaintMerge entity) {
        return new ComplaintMergeDto(
                entity.getId(), null, entity.getComplaint().getId(), entity.getMergedComplaintId(),
                entity.getMergedByUserId(), entity.getMergeReason(), entity.getMergedAt(),
                entity.getStatus(), true, 0L, null, null, null, null);
    }
}
