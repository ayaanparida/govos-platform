package com.govos.org.service;

import com.govos.org.dto.CreateDepartmentRequest;
import com.govos.org.dto.DepartmentDto;
import com.govos.org.dto.UpdateDepartmentRequest;
import com.govos.org.entity.Department;
import com.govos.org.entity.Organization;
import com.govos.org.exception.DepartmentNotFoundException;
import com.govos.org.exception.OrganizationNotFoundException;
import com.govos.org.mapper.DepartmentMapper;
import com.govos.org.repository.DepartmentRepository;
import com.govos.org.repository.OrganizationRepository;
import com.govos.org.validator.DepartmentTreeValidator;
import com.govos.org.validator.DepartmentValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentMapper departmentMapper;
    private final DepartmentValidator departmentValidator;
    private final DepartmentTreeValidator departmentTreeValidator;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            OrganizationRepository organizationRepository,
            DepartmentMapper departmentMapper,
            DepartmentValidator departmentValidator,
            DepartmentTreeValidator departmentTreeValidator) {
        this.departmentRepository = departmentRepository;
        this.organizationRepository = organizationRepository;
        this.departmentMapper = departmentMapper;
        this.departmentValidator = departmentValidator;
        this.departmentTreeValidator = departmentTreeValidator;
    }

    @Override
    public DepartmentDto getById(UUID id) {
        return departmentMapper.toDto(findActiveById(id));
    }

    @Override
    public List<DepartmentDto> getByOrganizationId(UUID organizationId) {
        return departmentRepository.findByOrganization_IdAndDeletedFalseOrderByNameAsc(organizationId).stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    @Override
    public List<DepartmentDto> getByParentDepartmentId(UUID parentDepartmentId) {
        return departmentRepository.findByParentDepartment_IdAndDeletedFalseOrderByNameAsc(parentDepartmentId).stream()
                .map(departmentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DepartmentDto create(CreateDepartmentRequest request) {
        departmentValidator.validateCreate(request);

        Organization organization = organizationRepository.findByIdAndDeletedFalse(request.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(request.organizationId()));

        Department entity = new Department();
        entity.setCode(request.code());
        entity.setOrganization(organization);
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        if (request.parentDepartmentId() != null) {
            Department parent = findActiveById(request.parentDepartmentId());
            departmentTreeValidator.validateParentAssignment(entity, parent);
            entity.setParentDepartment(parent);
        }

        return departmentMapper.toDto(departmentRepository.save(entity));
    }

    @Override
    @Transactional
    public DepartmentDto update(UUID id, UpdateDepartmentRequest request) {
        Department entity = findActiveById(id);
        assertVersion(entity, request.version());
        departmentValidator.validateUpdate(id, request);

        Organization organization = organizationRepository.findByIdAndDeletedFalse(request.organizationId())
                .orElseThrow(() -> new OrganizationNotFoundException(request.organizationId()));

        entity.setCode(request.code());
        entity.setOrganization(organization);
        entity.setName(request.name());
        entity.setDescription(request.description());
        if (request.active() != null) {
            entity.setActive(request.active());
        }
        entity.setParentDepartment(request.parentDepartmentId() != null
                ? resolveParent(entity, request.parentDepartmentId())
                : null);

        return departmentMapper.toDto(departmentRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Department entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        departmentRepository.save(entity);
    }

    private Department resolveParent(Department child, UUID parentDepartmentId) {
        Department parent = findActiveById(parentDepartmentId);
        departmentTreeValidator.validateParentAssignment(child, parent);
        return parent;
    }

    private Department findActiveById(UUID id) {
        return departmentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    private void assertVersion(Department entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Department version mismatch for id: " + entity.getId());
        }
    }
}
