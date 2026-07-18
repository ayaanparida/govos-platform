package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentDto;
import com.govos.cmp.entity.ComplaintAttachment;
import com.govos.cmp.mapper.ComplaintAttachmentMapper;
import com.govos.cmp.repository.ComplaintAttachmentRepository;
import com.govos.cmp.service.ComplaintAttachmentService;
import com.govos.cmp.validator.ComplaintAttachmentValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintAttachmentServiceImpl implements ComplaintAttachmentService {

    private final ComplaintAttachmentRepository complaintAttachmentRepository;
    private final ComplaintAttachmentMapper complaintAttachmentMapper;
    private final ComplaintAttachmentValidator complaintAttachmentValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintAttachmentServiceImpl(
            ComplaintAttachmentRepository complaintAttachmentRepository,
            ComplaintAttachmentMapper complaintAttachmentMapper,
            ComplaintAttachmentValidator complaintAttachmentValidator,
            ComplaintValidator complaintValidator) {
        this.complaintAttachmentRepository = complaintAttachmentRepository;
        this.complaintAttachmentMapper = complaintAttachmentMapper;
        this.complaintAttachmentValidator = complaintAttachmentValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintAttachmentDto addAttachment(ComplaintAttachmentCreateRequest request) {
        complaintAttachmentValidator.validateCreate(request);

        ComplaintAttachment entity = complaintAttachmentMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.complaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintAttachmentMapper.toDto(complaintAttachmentRepository.save(entity));
    }

    @Override
    public List<ComplaintAttachmentDto> listAttachments(UUID complaintId) {
        return complaintAttachmentRepository.findAllByComplaintIdAndDeletedFalse(complaintId).stream()
                .map(complaintAttachmentMapper::toDto)
                .toList();
    }
}
