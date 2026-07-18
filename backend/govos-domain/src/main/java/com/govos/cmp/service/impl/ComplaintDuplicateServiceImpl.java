package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateDto;
import com.govos.cmp.entity.ComplaintDuplicate;
import com.govos.cmp.mapper.ComplaintDuplicateMapper;
import com.govos.cmp.repository.ComplaintDuplicateRepository;
import com.govos.cmp.service.ComplaintDuplicateService;
import com.govos.cmp.validator.ComplaintDuplicateValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintDuplicateServiceImpl implements ComplaintDuplicateService {

    private final ComplaintDuplicateRepository complaintDuplicateRepository;
    private final ComplaintDuplicateMapper complaintDuplicateMapper;
    private final ComplaintDuplicateValidator complaintDuplicateValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintDuplicateServiceImpl(
            ComplaintDuplicateRepository complaintDuplicateRepository,
            ComplaintDuplicateMapper complaintDuplicateMapper,
            ComplaintDuplicateValidator complaintDuplicateValidator,
            ComplaintValidator complaintValidator) {
        this.complaintDuplicateRepository = complaintDuplicateRepository;
        this.complaintDuplicateMapper = complaintDuplicateMapper;
        this.complaintDuplicateValidator = complaintDuplicateValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintDuplicateDto createDuplicate(ComplaintDuplicateCreateRequest request) {
        complaintDuplicateValidator.validateCreate(request);

        ComplaintDuplicate entity = complaintDuplicateMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.primaryComplaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintDuplicateMapper.toDto(complaintDuplicateRepository.save(entity));
    }

    @Override
    public List<ComplaintDuplicateDto> listDuplicates(UUID primaryComplaintId) {
        return complaintDuplicateRepository.findAllByPrimaryComplaintIdAndDeletedFalse(primaryComplaintId).stream()
                .map(complaintDuplicateMapper::toDto)
                .toList();
    }
}
