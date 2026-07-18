package com.govos.doc.repository;

import com.govos.doc.entity.DocumentRetentionPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRetentionPolicyRepository extends JpaRepository<DocumentRetentionPolicy, UUID> {

    Optional<DocumentRetentionPolicy> findByIdAndDeletedFalse(UUID id);

    List<DocumentRetentionPolicy> findByOrganizationIdAndDeletedFalse(UUID organizationId);

    Optional<DocumentRetentionPolicy> findByNameAndDeletedFalse(String name);

    Optional<DocumentRetentionPolicy> findByOrganizationIdAndNameAndDeletedFalse(
            UUID organizationId, String name);
}
