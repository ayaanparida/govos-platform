package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.entity.ComplaintComment;
import com.govos.cmp.mapper.ComplaintCommentMapper;
import com.govos.cmp.repository.ComplaintCommentRepository;
import com.govos.cmp.service.ComplaintCommentService;
import com.govos.cmp.validator.ComplaintCommentValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintCommentServiceImpl implements ComplaintCommentService {

    private final ComplaintCommentRepository complaintCommentRepository;
    private final ComplaintCommentMapper complaintCommentMapper;
    private final ComplaintCommentValidator complaintCommentValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintCommentServiceImpl(
            ComplaintCommentRepository complaintCommentRepository,
            ComplaintCommentMapper complaintCommentMapper,
            ComplaintCommentValidator complaintCommentValidator,
            ComplaintValidator complaintValidator) {
        this.complaintCommentRepository = complaintCommentRepository;
        this.complaintCommentMapper = complaintCommentMapper;
        this.complaintCommentValidator = complaintCommentValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintCommentDto addComment(ComplaintCommentCreateRequest request) {
        complaintCommentValidator.validateCreate(request);

        ComplaintComment entity = complaintCommentMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.complaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintCommentMapper.toDto(complaintCommentRepository.save(entity));
    }

    @Override
    public List<ComplaintCommentDto> listComments(UUID complaintId) {
        return complaintCommentRepository.findAllByComplaintIdAndDeletedFalseOrderByCreatedDateAsc(complaintId)
                .stream()
                .map(complaintCommentMapper::toDto)
                .toList();
    }
}
