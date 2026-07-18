package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.entity.ComplaintFeedback;
import com.govos.cmp.enums.ComplaintFeedbackRating;
import com.govos.cmp.exception.ComplaintFeedbackException;
import com.govos.cmp.mapper.ComplaintFeedbackMapper;
import com.govos.cmp.repository.ComplaintFeedbackRepository;
import com.govos.cmp.support.CmpTestFixtures;
import com.govos.cmp.validator.ComplaintFeedbackValidator;
import com.govos.cmp.validator.ComplaintValidator;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintFeedbackServiceImplTest {

    @Mock private ComplaintFeedbackRepository complaintFeedbackRepository;
    @Mock private ComplaintFeedbackMapper complaintFeedbackMapper;
    @Mock private ComplaintFeedbackValidator complaintFeedbackValidator;
    @Mock private ComplaintValidator complaintValidator;

    @InjectMocks
    private ComplaintFeedbackServiceImpl service;

    @Test
    void shouldCreateFeedback() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedbackCreateRequest request = CmpTestFixtures.feedbackCreateRequest(complaintId);
        ComplaintFeedback entity = CmpTestFixtures.feedback(UUID.randomUUID(), complaintId);
        ComplaintFeedbackDto dto = feedbackDto(entity);

        when(complaintFeedbackMapper.toEntity(request)).thenReturn(entity);
        when(complaintValidator.requireExists(complaintId))
                .thenReturn(CmpTestFixtures.complaint(complaintId, entity.getComplaint().getStatus()));
        when(complaintFeedbackRepository.save(entity)).thenReturn(entity);
        when(complaintFeedbackMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.createFeedback(request)).isEqualTo(dto);
        verify(complaintFeedbackValidator).validateCreate(request);
    }

    @Test
    void shouldUpdateFeedback() {
        UUID complaintId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();
        ComplaintFeedback entity = CmpTestFixtures.feedback(feedbackId, complaintId);
        ComplaintFeedbackUpdateRequest request = CmpTestFixtures.feedbackUpdateRequest();
        ComplaintFeedbackDto dto = feedbackDto(entity);

        when(complaintFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(entity));
        when(complaintFeedbackRepository.save(entity)).thenReturn(entity);
        when(complaintFeedbackMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.updateFeedback(feedbackId, request)).isEqualTo(dto);
        verify(complaintFeedbackValidator).validateUpdate(feedbackId, complaintId, request);
        verify(complaintFeedbackMapper).updateEntity(request, entity);
    }

    @Test
    void shouldThrowWhenUpdateVersionMismatch() {
        UUID feedbackId = UUID.randomUUID();
        ComplaintFeedback entity = CmpTestFixtures.feedback(feedbackId, UUID.randomUUID());
        entity.setVersion(2L);
        ComplaintFeedbackUpdateRequest request = new ComplaintFeedbackUpdateRequest(
                ComplaintFeedbackRating.FOUR, null, true, 0L);

        when(complaintFeedbackRepository.findById(feedbackId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.updateFeedback(feedbackId, request))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void shouldGetFeedback() {
        UUID complaintId = UUID.randomUUID();
        ComplaintFeedback entity = CmpTestFixtures.feedback(UUID.randomUUID(), complaintId);
        ComplaintFeedbackDto dto = feedbackDto(entity);

        when(complaintFeedbackRepository.findByComplaintIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.of(entity));
        when(complaintFeedbackMapper.toDto(entity)).thenReturn(dto);

        assertThat(service.getFeedback(complaintId)).isEqualTo(dto);
    }

    @Test
    void shouldThrowWhenFeedbackNotFound() {
        UUID complaintId = UUID.randomUUID();
        when(complaintFeedbackRepository.findByComplaintIdAndDeletedFalse(complaintId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getFeedback(complaintId))
                .isInstanceOf(ComplaintFeedbackException.class);
    }

    private static ComplaintFeedbackDto feedbackDto(ComplaintFeedback entity) {
        return new ComplaintFeedbackDto(
                entity.getId(), null, entity.getComplaint().getId(), entity.getRatedByUserId(),
                entity.getRating(), entity.getFeedback(), entity.getRatedAt(), true, 0L,
                null, null, null, null);
    }
}
