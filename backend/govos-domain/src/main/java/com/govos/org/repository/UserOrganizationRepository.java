package com.govos.org.repository;

import com.govos.org.entity.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, UUID> {

    Optional<UserOrganization> findByIdAndDeletedFalse(UUID id);

    List<UserOrganization> findByUser_IdAndDeletedFalse(UUID userId);

    Optional<UserOrganization> findByUser_IdAndDefaultOrganizationTrueAndDeletedFalse(UUID userId);

    boolean existsByUser_IdAndOrganization_IdAndDeletedFalse(UUID userId, UUID organizationId);
}
