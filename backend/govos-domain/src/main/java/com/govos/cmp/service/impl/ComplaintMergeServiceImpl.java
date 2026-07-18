package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;
import com.govos.cmp.entity.ComplaintMerge;
import com.govos.cmp.mapper.ComplaintMergeMapper;
import com.govos.cmp.repository.ComplaintMergeRepository;
import com.govos.cmp.service.ComplaintMergeService;
import com.govos.cmp.validator.ComplaintMergeValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintMergeServiceImpl implements ComplaintMergeService {

    private final ComplaintMergeRepository complaintMergeRepository;
    private final ComplaintMergeMapper complaintMergeMapper;
    private final ComplaintMergeValidator complaintMergeValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintMergeServiceImpl(
            ComplaintMergeRepository complaintMergeRepository,
            ComplaintMergeMapper complaintMergeMapper,
            ComplaintMergeValidator complaintMergeValidator,
            ComplaintValidator complaintValidator) {
        this.complaintMergeRepository = complaintMergeRepository;
        this.complaintMergeMapper = complaintMergeMapper;
        this.complaintMergeValidator = complaintMergeValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintMergeDto createMerge(ComplaintMergeCreateRequest request) {
        complaintMergeValidator.validateCreate(request);

        ComplaintMerge entity = complaintMergeMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.survivingComplaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintMergeMapper.toDto(complaintMergeRepository.save(entity));
    }

    @Override
    public List<ComplaintMergeDto> listMerges(UUID survivingComplaintId) {
        return complaintMergeRepository.findAllBySurvivingComplaintIdAndDeletedFalse(survivingComplaintId).stream()
                .map(complaintMergeMapper::toDto)
                .toList();
    }
}
