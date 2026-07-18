package com.govos.audit.repository;

import com.govos.audit.entity.AuditEvent;
import com.govos.audit.entity.AuditEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    Optional<AuditEvent> findByIdAndDeletedFalse(UUID id);

    Optional<AuditEvent> findByEventCodeAndDeletedFalse(String eventCode);

    boolean existsByEventCodeAndDeletedFalse(String eventCode);

    List<AuditEvent> findByEntityTypeAndEntityIdAndDeletedFalseOrderByEventTimestampDesc(
            String entityType, UUID entityId);

    List<AuditEvent> findByActor_IdAndDeletedFalseOrderByEventTimestampDesc(UUID actorId);

    List<AuditEvent> findBySession_IdAndDeletedFalseOrderByEventTimestampDesc(UUID sessionId);

    List<AuditEvent> findByStatusAndDeletedFalseOrderByEventTimestampDesc(AuditEventStatus status);
}
