package com.govos.org.service;

import com.govos.org.dto.CreateOfficeRequest;
import com.govos.org.dto.OfficeDto;
import com.govos.org.dto.UpdateOfficeRequest;
import com.govos.org.entity.Department;
import com.govos.org.entity.Office;
import com.govos.org.exception.DepartmentNotFoundException;
import com.govos.org.exception.OfficeNotFoundException;
import com.govos.org.mapper.OfficeMapper;
import com.govos.org.repository.DepartmentRepository;
import com.govos.org.repository.OfficeRepository;
import com.govos.org.validator.OfficeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OfficeServiceImpl implements OfficeService {

    private final OfficeRepository officeRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficeMapper officeMapper;
    private final OfficeValidator officeValidator;

    public OfficeServiceImpl(
            OfficeRepository officeRepository,
            DepartmentRepository departmentRepository,
            OfficeMapper officeMapper,
            OfficeValidator officeValidator) {
        this.officeRepository = officeRepository;
        this.departmentRepository = departmentRepository;
        this.officeMapper = officeMapper;
        this.officeValidator = officeValidator;
    }

    @Override
    public OfficeDto getById(UUID id) {
        return officeMapper.toDto(findActiveById(id));
    }

    @Override
    public List<OfficeDto> getByDepartmentId(UUID departmentId) {
        return officeRepository.findByDepartment_IdAndDeletedFalseOrderByOfficeNameAsc(departmentId).stream()
                .map(officeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public OfficeDto create(CreateOfficeRequest request) {
        officeValidator.validateCreate(request);

        Department department = departmentRepository.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));

        Office entity = new Office();
        entity.setCode(request.code());
        entity.setDepartment(department);
        entity.setOfficeName(request.officeName());
        entity.setAddress(request.address());
        entity.setDistrict(request.district());
        entity.setState(request.state());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return officeMapper.toDto(officeRepository.save(entity));
    }

    @Override
    @Transactional
    public OfficeDto update(UUID id, UpdateOfficeRequest request) {
        Office entity = findActiveById(id);
        assertVersion(entity, request.version());
        officeValidator.validateUpdate(id, request);

        Department department = departmentRepository.findByIdAndDeletedFalse(request.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(request.departmentId()));

        entity.setCode(request.code());
        entity.setDepartment(department);
        entity.setOfficeName(request.officeName());
        entity.setAddress(request.address());
        entity.setDistrict(request.district());
        entity.setState(request.state());
        entity.setLatitude(request.latitude());
        entity.setLongitude(request.longitude());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return officeMapper.toDto(officeRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Office entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        officeRepository.save(entity);
    }

    private Office findActiveById(UUID id) {
        return officeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new OfficeNotFoundException(id));
    }

    private void assertVersion(Office entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Office version mismatch for id: " + entity.getId());
        }
    }
}
