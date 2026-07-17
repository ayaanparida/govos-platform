package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;
import com.govos.cmp.entity.ComplaintAssignment;
import com.govos.cmp.mapper.ComplaintAssignmentMapper;
import com.govos.cmp.repository.ComplaintAssignmentRepository;
import com.govos.cmp.service.ComplaintAssignmentService;
import com.govos.cmp.validator.ComplaintAssignmentValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintAssignmentServiceImpl implements ComplaintAssignmentService {

    private final ComplaintAssignmentRepository complaintAssignmentRepository;
    private final ComplaintAssignmentMapper complaintAssignmentMapper;
    private final ComplaintAssignmentValidator complaintAssignmentValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintAssignmentServiceImpl(
            ComplaintAssignmentRepository complaintAssignmentRepository,
            ComplaintAssignmentMapper complaintAssignmentMapper,
            ComplaintAssignmentValidator complaintAssignmentValidator,
            ComplaintValidator complaintValidator) {
        this.complaintAssignmentRepository = complaintAssignmentRepository;
        this.complaintAssignmentMapper = complaintAssignmentMapper;
        this.complaintAssignmentValidator = complaintAssignmentValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintAssignmentDto createAssignment(ComplaintAssignmentCreateRequest request) {
        complaintAssignmentValidator.validateCreate(request);

        ComplaintAssignment entity = complaintAssignmentMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.complaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintAssignmentMapper.toDto(complaintAssignmentRepository.save(entity));
    }

    @Override
    public Optional<ComplaintAssignmentDto> getCurrentAssignment(UUID complaintId) {
        return complaintAssignmentRepository.findByComplaintIdAndIsCurrentTrueAndDeletedFalse(complaintId)
                .map(complaintAssignmentMapper::toDto);
    }

    @Override
    public List<ComplaintAssignmentDto> listAssignments(UUID complaintId) {
        return complaintAssignmentRepository.findAllByComplaintIdAndDeletedFalse(complaintId).stream()
                .map(complaintAssignmentMapper::toDto)
                .toList();
    }
}
