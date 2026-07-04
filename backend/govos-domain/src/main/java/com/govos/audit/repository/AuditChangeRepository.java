package com.govos.audit.repository;

import com.govos.audit.entity.AuditChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditChangeRepository extends JpaRepository<AuditChange, UUID> {

    Optional<AuditChange> findByIdAndDeletedFalse(UUID id);

    List<AuditChange> findByAuditEvent_IdAndDeletedFalseOrderByFieldNameAsc(UUID auditEventId);

    boolean existsByAuditEvent_IdAndFieldNameAndDeletedFalse(UUID auditEventId, String fieldName);
}
