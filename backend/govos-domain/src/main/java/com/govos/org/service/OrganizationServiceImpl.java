package com.govos.org.service;

import com.govos.org.dto.CreateOrganizationRequest;
import com.govos.org.dto.OrganizationDto;
import com.govos.org.dto.UpdateOrganizationRequest;
import com.govos.org.entity.Organization;
import com.govos.org.entity.OrganizationStatus;
import com.govos.org.exception.OrganizationNotFoundException;
import com.govos.org.mapper.OrganizationMapper;
import com.govos.org.repository.OrganizationRepository;
import com.govos.org.validator.OrganizationValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final OrganizationValidator organizationValidator;

    public OrganizationServiceImpl(
            OrganizationRepository organizationRepository,
            OrganizationMapper organizationMapper,
            OrganizationValidator organizationValidator) {
        this.organizationRepository = organizationRepository;
        this.organizationMapper = organizationMapper;
        this.organizationValidator = organizationValidator;
    }

    @Override
    public OrganizationDto getById(UUID id) {
        return organizationMapper.toDto(findActiveById(id));
    }

    @Override
    public OrganizationDto getByCode(String code) {
        return organizationMapper.toDto(findActiveByCode(code));
    }

    @Override
    public List<OrganizationDto> getAll() {
        return organizationRepository.findByDeletedFalseOrderByNameAsc().stream()
                .map(organizationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OrganizationDto create(CreateOrganizationRequest request) {
        organizationValidator.validateCreate(request);

        Organization entity = organizationMapper.toEntity(request);
        applyDefaults(entity, request.active(), request.status());

        return organizationMapper.toDto(organizationRepository.save(entity));
    }

    @Override
    @Transactional
    public OrganizationDto update(UUID id, UpdateOrganizationRequest request) {
        Organization entity = findActiveById(id);
        assertVersion(entity, request.version());

        organizationValidator.validateUpdate(id, request);
        organizationMapper.updateEntity(request, entity);
        applyDefaults(entity, request.active(), request.status());

        return organizationMapper.toDto(organizationRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Organization entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        organizationRepository.save(entity);
    }

    private Organization findActiveById(UUID id) {
        return organizationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    private Organization findActiveByCode(String code) {
        return organizationRepository.findByCodeAndDeletedFalse(code)
                .orElseThrow(() -> new OrganizationNotFoundException(code));
    }

    private void applyDefaults(Organization entity, Boolean active, OrganizationStatus status) {
        if (active != null) {
            entity.setActive(active);
        } else if (entity.getActive() == null) {
            entity.setActive(true);
        }
        if (status != null) {
            entity.setStatus(status);
        } else if (entity.getStatus() == null) {
            entity.setStatus(OrganizationStatus.PENDING);
        }
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
    }

    private void assertVersion(Organization entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Organization version mismatch for id: " + entity.getId());
        }
    }
}
