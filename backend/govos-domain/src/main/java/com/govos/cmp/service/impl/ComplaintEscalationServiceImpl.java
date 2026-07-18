package com.govos.cmp.service.impl;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.entity.ComplaintEscalation;
import com.govos.cmp.mapper.ComplaintEscalationMapper;
import com.govos.cmp.repository.ComplaintEscalationRepository;
import com.govos.cmp.service.ComplaintEscalationService;
import com.govos.cmp.validator.ComplaintEscalationValidator;
import com.govos.cmp.validator.ComplaintValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ComplaintEscalationServiceImpl implements ComplaintEscalationService {

    private final ComplaintEscalationRepository complaintEscalationRepository;
    private final ComplaintEscalationMapper complaintEscalationMapper;
    private final ComplaintEscalationValidator complaintEscalationValidator;
    private final ComplaintValidator complaintValidator;

    public ComplaintEscalationServiceImpl(
            ComplaintEscalationRepository complaintEscalationRepository,
            ComplaintEscalationMapper complaintEscalationMapper,
            ComplaintEscalationValidator complaintEscalationValidator,
            ComplaintValidator complaintValidator) {
        this.complaintEscalationRepository = complaintEscalationRepository;
        this.complaintEscalationMapper = complaintEscalationMapper;
        this.complaintEscalationValidator = complaintEscalationValidator;
        this.complaintValidator = complaintValidator;
    }

    @Override
    @Transactional
    public ComplaintEscalationDto createEscalation(ComplaintEscalationCreateRequest request) {
        complaintEscalationValidator.validateCreate(request);

        ComplaintEscalation entity = complaintEscalationMapper.toEntity(request);
        entity.setComplaint(complaintValidator.requireExists(request.complaintId()));
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return complaintEscalationMapper.toDto(complaintEscalationRepository.save(entity));
    }

    @Override
    public List<ComplaintEscalationDto> listEscalations(UUID complaintId) {
        return complaintEscalationRepository.findAllByComplaintIdAndDeletedFalseOrderByEscalatedAtAsc(complaintId)
                .stream()
                .map(complaintEscalationMapper::toDto)
                .toList();
    }
}
