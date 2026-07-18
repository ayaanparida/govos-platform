package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateDto;
import com.govos.cmp.entity.ComplaintDuplicate;
import com.govos.cmp.mapper.ComplaintDuplicateMapper;
import com.govos.cmp.repository.ComplaintDuplicateRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintDuplicateValidator;
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
class ComplaintDuplicateServiceImplTest {

    @Mock private ComplaintDuplicateRepository complaintDuplicateRepository;
    @Mock private ComplaintDuplicateMapper complaintDuplicateMapper;
    @Mock private ComplaintDuplicateValidator complaintDuplicateValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintDuplicateServiceImpl service;

    @Test
    void shouldCreateDuplicateLink() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicateCreateRequest request = CmpTestFixtures.duplicateCreateRequest(primaryId, duplicateId);
        ComplaintDuplicate entity = CmpTestFixtures.duplicateLink(UUID.randomUUID(), primaryId, duplicateId);
        ComplaintDuplicateDto dto = duplicateDto(entity);

        when(complaintDuplicateMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(primaryId))
                .thenReturn(CmpTestFixtures.complaint(primaryId, entity.getComplaint().getStatus()));
        when(complaintDuplicateRepository.save(entity)).thenReturn(entity);
        when(complaintDuplicateMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.createDuplicate(request)).isEqualTo(dto);
        verify(complaintDuplicateValidator).validateCreate(request);
    }

    @Test
    void shouldListDuplicates() {
        UUID primaryId = UUID.randomUUID();
        UUID duplicateId = UUID.randomUUID();
        ComplaintDuplicate entity = CmpTestFixtures.duplicateLink(UUID.randomUUID(), primaryId, duplicateId);
        ComplaintDuplicateDto dto = duplicateDto(entity);

        when(complaintDuplicateRepository.findAllByPrimaryComplaintIdAndDeletedFalse(primaryId))
                .thenReturn(List.of(entity));
        when(complaintDuplicateMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.listDuplicates(primaryId)).containsExactly(dto);
    }

    private static ComplaintDuplicateDto duplicateDto(ComplaintDuplicate entity) {
        return new ComplaintDuplicateDto(
                entity.getId(), null, entity.getComplaint().getId(), entity.getDuplicateComplaintId(),
                entity.getDetectedBy(), entity.getDetectedByUserId(), entity.getSimilarityScore(),
                entity.getRemarks(), entity.getStatus(), true, 0L, null, null, null, null);
    }
}
