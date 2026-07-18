package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.entity.ComplaintFeedback;
import com.govos.cmp.exception.ComplaintFeedbackException;
import com.govos.cmp.mapper.ComplaintFeedbackMapper;
import com.govos.cmp.repository.ComplaintFeedbackRepository;
import com.govos.cmp.service.ComplaintFeedbackService;
import com.govos.cmp.validator.ComplaintFeedbackValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintFeedbackServiceImpl implements ComplaintFeedbackService {

    private final ComplaintFeedbackRepository complaintFeedbackRepository;
    private final ComplaintFeedbackMapper complaintFeedbackMapper;
    private final ComplaintFeedbackValidator complaintFeedbackValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintFeedbackServiceImpl(
            ComplaintFeedbackRepository complaintFeedbackRepository,
            ComplaintFeedbackMapper complaintFeedbackMapper,
            ComplaintFeedbackValidator complaintFeedbackValidator,
            ComplaintValidator complaintValidator) {
        this.complaintFeedbackRepository = complaintFeedbackRepository;
        this.complaintFeedbackMapper = complaintFeedbackMapper;
        this.complaintFeedbackValidator = complaintFeedbackValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto createFeedback(ComplaintFeedbackCreateRequest request) {
        complaintFeedbackValidator.validateCreate(request);

        ComplaintFeedback entity = complaintFeedbackMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.complaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintFeedbackMapper.toDto(complaintFeedbackRepository.save(entity));
    }

    @Override
    @Transactional
    public ComplaintFeedbackDto updateFeedback(UUID id, ComplaintFeedbackUpdateRequest request) {
        ComplaintFeedback entity = findActiveById(id);
        assertVersion(entity, request.version());

        complaintFeedbackValidator.validateUpdate(id, entity.getComplaint().getId(), request);
        complaintFeedbackMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return complaintFeedbackMapper.toDto(complaintFeedbackRepository.save(entity));
    }

    @Override
    public ComplaintFeedbackDto getFeedback(UUID complaintId) {
        return complaintFeedbackRepository.findByComplaintIdAndDeletedFalse(complaintId)
                .map(complaintFeedbackMapper::toDto)
                .orElseThrow(() -> new ComplaintFeedbackException(
                        "Feedback not found for complaint: " + complaintId));
    }

    private ComplaintFeedback findActiveById(UUID id) {
        return complaintFeedbackRepository.findById(id)
                .filter(feedback -> !Boolean.TRUE.equals(feedback.getDeleted()))
                .orElseThrow(() -> new ComplaintFeedbackException("Feedback not found with id: " + id));
    }

    private void assertVersion(ComplaintFeedback entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "ComplaintFeedback version mismatch for id: " + entity.getId());
        }
    }
}
