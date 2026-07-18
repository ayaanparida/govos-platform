package com.govos.org.repository;

import com.govos.org.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Optional<Department> findByIdAndDeletedFalse(UUID id);

    Optional<Department> findByOrganization_IdAndCodeAndDeletedFalse(UUID organizationId, String code);

    List<Department> findByOrganization_IdAndDeletedFalseOrderByNameAsc(UUID organizationId);

    List<Department> findByParentDepartment_IdAndDeletedFalseOrderByNameAsc(UUID parentDepartmentId);

    boolean existsByOrganization_IdAndCodeAndDeletedFalse(UUID organizationId, String code);
}
