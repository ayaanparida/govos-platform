package com.govos.org.repository;

import com.govos.org.entity.Organization;
import com.govos.org.entity.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    Optional<Organization> findByIdAndDeletedFalse(UUID id);

    Optional<Organization> findByCodeAndDeletedFalse(String code);

    List<Organization> findByDeletedFalseOrderByNameAsc();

    List<Organization> findByStatusAndDeletedFalseOrderByNameAsc(OrganizationStatus status);

    boolean existsByCodeAndDeletedFalse(String code);
}
