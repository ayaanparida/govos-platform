package com.govos.org.repository;

import com.govos.org.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, UUID> {

    Optional<Designation> findByIdAndDeletedFalse(UUID id);

    Optional<Designation> findByCodeAndDeletedFalse(String code);

    List<Designation> findByDeletedFalseOrderByTitleAsc();

    boolean existsByCodeAndDeletedFalse(String code);
}
