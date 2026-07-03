package com.govos.org.repository;

import com.govos.org.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    Optional<Employee> findByEmployeeNumberAndDeletedFalse(String employeeNumber);

    List<Employee> findByOrganization_IdAndDeletedFalseOrderByEmployeeNumberAsc(UUID organizationId);

    List<Employee> findByUser_IdAndDeletedFalse(UUID userId);

    boolean existsByEmployeeNumberAndDeletedFalse(String employeeNumber);
}
