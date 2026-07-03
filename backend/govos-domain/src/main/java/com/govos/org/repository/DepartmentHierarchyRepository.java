package com.govos.org.repository;

import com.govos.org.entity.DepartmentHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentHierarchyRepository extends JpaRepository<DepartmentHierarchy, UUID> {

    Optional<DepartmentHierarchy> findByIdAndDeletedFalse(UUID id);

    List<DepartmentHierarchy> findByParentDepartment_IdAndDeletedFalse(UUID parentDepartmentId);

    List<DepartmentHierarchy> findByChildDepartment_IdAndDeletedFalse(UUID childDepartmentId);

    boolean existsByParentDepartment_IdAndChildDepartment_IdAndDeletedFalse(
            UUID parentDepartmentId, UUID childDepartmentId);
}
