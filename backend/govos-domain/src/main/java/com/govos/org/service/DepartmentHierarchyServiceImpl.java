package com.govos.org.service;

import com.govos.org.dto.CreateDepartmentHierarchyRequest;
import com.govos.org.dto.DepartmentHierarchyDto;
import com.govos.org.entity.Department;
import com.govos.org.entity.DepartmentHierarchy;
import com.govos.org.exception.DepartmentHierarchyNotFoundException;
import com.govos.org.exception.DepartmentNotFoundException;
import com.govos.org.exception.DuplicateAssignmentException;
import com.govos.org.mapper.DepartmentHierarchyMapper;
import com.govos.org.repository.DepartmentHierarchyRepository;
import com.govos.org.repository.DepartmentRepository;
import com.govos.org.validator.DepartmentTreeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DepartmentHierarchyServiceImpl implements DepartmentHierarchyService {

    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentHierarchyMapper departmentHierarchyMapper;
    private final DepartmentTreeValidator departmentTreeValidator;

    public DepartmentHierarchyServiceImpl(
            DepartmentHierarchyRepository departmentHierarchyRepository,
            DepartmentRepository departmentRepository,
            DepartmentHierarchyMapper departmentHierarchyMapper,
            DepartmentTreeValidator departmentTreeValidator) {
        this.departmentHierarchyRepository = departmentHierarchyRepository;
        this.departmentRepository = departmentRepository;
        this.departmentHierarchyMapper = departmentHierarchyMapper;
        this.departmentTreeValidator = departmentTreeValidator;
    }

    @Override
    public DepartmentHierarchyDto getById(UUID id) {
        return departmentHierarchyMapper.toDto(findActiveById(id));
    }

    @Override
    public List<DepartmentHierarchyDto> getByParentDepartmentId(UUID parentDepartmentId) {
        return departmentHierarchyRepository.findByParentDepartment_IdAndDeletedFalse(parentDepartmentId).stream()
                .map(departmentHierarchyMapper::toDto)
                .toList();
    }

    @Override
    public List<DepartmentHierarchyDto> getByChildDepartmentId(UUID childDepartmentId) {
        return departmentHierarchyRepository.findByChildDepartment_IdAndDeletedFalse(childDepartmentId).stream()
                .map(departmentHierarchyMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DepartmentHierarchyDto create(CreateDepartmentHierarchyRequest request) {
        if (departmentHierarchyRepository.existsByParentDepartment_IdAndChildDepartment_IdAndDeletedFalse(
                request.parentDepartmentId(), request.childDepartmentId())) {
            throw new DuplicateAssignmentException(
                    "Hierarchy edge already exists for parent=" + request.parentDepartmentId()
                            + ", child=" + request.childDepartmentId());
        }

        Department parent = departmentRepository.findByIdAndDeletedFalse(request.parentDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.parentDepartmentId()));
        Department child = departmentRepository.findByIdAndDeletedFalse(request.childDepartmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.childDepartmentId()));

        departmentTreeValidator.validateHierarchyEdge(parent, child);

        DepartmentHierarchy entity = new DepartmentHierarchy();
        entity.setParentDepartment(parent);
        entity.setChildDepartment(child);
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return departmentHierarchyMapper.toDto(departmentHierarchyRepository.save(entity));
    }

    @Override
    @Transactional
    public void remove(UUID id) {
        DepartmentHierarchy entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        departmentHierarchyRepository.save(entity);
    }

    private DepartmentHierarchy findActiveById(UUID id) {
        return departmentHierarchyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new DepartmentHierarchyNotFoundException(id));
    }
}
