package com.govos.org.repository;

import com.govos.org.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfficeRepository extends JpaRepository<Office, UUID> {

    Optional<Office> findByIdAndDeletedFalse(UUID id);

    Optional<Office> findByDepartment_IdAndCodeAndDeletedFalse(UUID departmentId, String code);

    List<Office> findByDepartment_IdAndDeletedFalseOrderByOfficeNameAsc(UUID departmentId);

    boolean existsByDepartment_IdAndCodeAndDeletedFalse(UUID departmentId, String code);
}
