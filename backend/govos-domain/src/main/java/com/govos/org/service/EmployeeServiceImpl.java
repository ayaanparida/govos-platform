package com.govos.org.service;

import com.govos.idm.entity.User;
import com.govos.idm.exception.UserNotFoundException;
import com.govos.idm.repository.UserRepository;
import com.govos.org.dto.CreateEmployeeRequest;
import com.govos.org.dto.EmployeeDto;
import com.govos.org.dto.UpdateEmployeeRequest;
import com.govos.org.entity.Department;
import com.govos.org.entity.Designation;
import com.govos.org.entity.Employee;
import com.govos.org.entity.Office;
import com.govos.org.entity.Organization;
import com.govos.org.exception.DepartmentNotFoundException;
import com.govos.org.exception.DesignationNotFoundException;
import com.govos.org.exception.EmployeeNotFoundException;
import com.govos.org.exception.OfficeNotFoundException;
import com.govos.org.exception.OrganizationNotFoundException;
import com.govos.org.mapper.EmployeeMapper;
import com.govos.org.repository.DepartmentRepository;
import com.govos.org.repository.DesignationRepository;
import com.govos.org.repository.EmployeeRepository;
import com.govos.org.repository.OfficeRepository;
import com.govos.org.repository.OrganizationRepository;
import com.govos.org.validator.EmployeeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final OfficeRepository officeRepository;
    private final DesignationRepository designationRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeValidator employeeValidator;
    private final EmployeeNumberGenerator employeeNumberGenerator;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            OfficeRepository officeRepository,
            DesignationRepository designationRepository,
            EmployeeMapper employeeMapper,
            EmployeeValidator employeeValidator,
            EmployeeNumberGenerator employeeNumberGenerator) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.officeRepository = officeRepository;
        this.designationRepository = designationRepository;
        this.employeeMapper = employeeMapper;
        this.employeeValidator = employeeValidator;
        this.employeeNumberGenerator = employeeNumberGenerator;
    }

    @Override
    public EmployeeDto getById(UUID id) {
        return employeeMapper.toDto(findActiveById(id));
    }

    @Override
    public EmployeeDto getByEmployeeNumber(String employeeNumber) {
        return employeeMapper.toDto(employeeRepository.findByEmployeeNumberAndDeletedFalse(employeeNumber)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeNumber)));
    }

    @Override
    public List<EmployeeDto> getByOrganizationId(UUID organizationId) {
        return employeeRepository.findByOrganization_IdAndDeletedFalseOrderByEmployeeNumberAsc(organizationId).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    public List<EmployeeDto> getByUserId(UUID userId) {
        return employeeRepository.findByUser_IdAndDeletedFalse(userId).stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EmployeeDto create(CreateEmployeeRequest request) {
        Employee entity = buildEmployee(request.userId(), request.organizationId(), request.departmentId(),
                request.officeId(), request.designationId());
        entity.setCode(request.code());
        entity.setEmployeeNumber(employeeNumberGenerator.generateNext());
        entity.setJoiningDate(request.joiningDate());
        entity.setRetirementDate(request.retirementDate());
        entity.setOfficialEmail(request.officialEmail());
        entity.setOfficialMobile(request.officialMobile());
        entity.setActive(request.active() != null ? request.active() : true);
        entity.setDeleted(false);

        return employeeMapper.toDto(employeeRepository.save(entity));
    }

    @Override
    @Transactional
    public EmployeeDto update(UUID id, UpdateEmployeeRequest request) {
        Employee entity = findActiveById(id);
        assertVersion(entity, request.version());
        employeeValidator.validateUpdate(id, request);

        applyReferences(entity, request.userId(), request.organizationId(), request.departmentId(),
                request.officeId(), request.designationId());
        entity.setCode(request.code());
        entity.setJoiningDate(request.joiningDate());
        entity.setRetirementDate(request.retirementDate());
        entity.setOfficialEmail(request.officialEmail());
        entity.setOfficialMobile(request.officialMobile());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        return employeeMapper.toDto(employeeRepository.save(entity));
    }

    @Override
    @Transactional
    public void softDelete(UUID id) {
        Employee entity = findActiveById(id);
        entity.setDeleted(true);
        entity.setActive(false);
        employeeRepository.save(entity);
    }

    private Employee buildEmployee(UUID userId, UUID organizationId, UUID departmentId,
                                   UUID officeId, UUID designationId) {
        Employee entity = new Employee();
        applyReferences(entity, userId, organizationId, departmentId, officeId, designationId);
        return entity;
    }

    private void applyReferences(Employee entity, UUID userId, UUID organizationId, UUID departmentId,
                               UUID officeId, UUID designationId) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Organization organization = organizationRepository.findByIdAndDeletedFalse(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(organizationId));
        Department department = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        Designation designation = designationRepository.findByIdAndDeletedFalse(designationId)
                .orElseThrow(() -> new DesignationNotFoundException(designationId));

        entity.setUser(user);
        entity.setOrganization(organization);
        entity.setDepartment(department);
        entity.setDesignation(designation);
        entity.setOffice(officeId != null
                ? officeRepository.findByIdAndDeletedFalse(officeId)
                        .orElseThrow(() -> new OfficeNotFoundException(officeId))
                : null);
    }

    private Employee findActiveById(UUID id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    private void assertVersion(Employee entity, Long version) {
        if (version != null && !version.equals(entity.getVersion())) {
            throw new jakarta.persistence.OptimisticLockException(
                    "Employee version mismatch for id: " + entity.getId());
        }
    }
}
