package com.govos.audit.repository;

import com.govos.audit.entity.AuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditEntityRepository extends JpaRepository<AuditEntity, UUID> {

    Optional<AuditEntity> findByIdAndDeletedFalse(UUID id);

    Optional<AuditEntity> findByEntityTypeAndEntityIdAndDeletedFalse(String entityType, UUID entityId);

    boolean existsByEntityTypeAndEntityIdAndDeletedFalse(String entityType, UUID entityId);

    List<AuditEntity> findByEntityTypeAndDeletedFalseOrderByEntityNameAsc(String entityType);
}
