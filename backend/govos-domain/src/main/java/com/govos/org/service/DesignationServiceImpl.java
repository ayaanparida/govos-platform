package com.govos.org.service;

import com.govos.org.dto.CreateDesignationRequest;
import com.govos.org.dto.DesignationDto;
import com.govos.org.dto.UpdateDesignationRequest;
import com.govos.org.entity.Designation;
import com.govos.org.exception.DesignationNotFoundException;
import com.govos.org.mapper.DesignationMapper;
import com.govos.org.repository.DesignationRepository;
import com.govos.org.validator.DesignationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DesignationServiceImpl implements DesignationService {

    private final DesignationRepository designationRepository;
    private final DesignationMapper designationMapper;
    private final DesignationValidator designationValidator;

    public DesignationServiceImpl(
            DesignationRepository designationRepository,
            DesignationMapper designationMapper,
            DesignationValidator designationValidator) {
        this.designationRepository = designationRepository;
        this.designationMapper = designationMapper;
        this.designationValidator = designationValidator;
    }

    @Override
    public DesignationDto getById(UUID id) {
        return designationMapper.toDto(findActiveById(id));
    }

    @Override
    public DesignationDto getByCode(String code) {
        return designationMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<DesignationDto> getAll() {
        return designationRepository.findByDeletedFalseOrderByTitleAsc().stream()
                .map(designationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DesignationDto create(CreateDesignationRequest request) {
        designationValidator.validateCreate(request);

        Designation entity = designationMapper.toEntity(request);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return designationMapper.toDto(designationRepository.save(entity));
    }

    @Override
    @Transactional
    public DesignationDto update(UUID id, UpdateDesignationRequest request) {
        Designation entity = findActiveById(id);
        assertVersion(entity, request.version());

        designationValidator.validateUpdate(id, request);
        designationMapper.updateEntity(request, entity);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return designationMapper.toDto(designationRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Designation entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        designationRepository.save(entity);
    }

    private Designation findActiveById(UUID id) {
        return designationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DesignationNotFoundException(id));
    }

    private Designation findActiveByCode(String code) {
        return designationRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new DesignationNotFoundException(code));
    }

    private void assertVersion(Designation entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Designation version mismatch for id: " + entity.getId());
        }
    }
}
