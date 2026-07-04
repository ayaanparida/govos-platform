package com.govos.audit.repository;

import com.govos.audit.entity.AuditSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditSessionRepository extends JpaRepository<AuditSession, UUID> {

    Optional<AuditSession> findByIdAndDeletedFalse(UUID id);

    Optional<AuditSession> findBySessionIdAndDeletedFalse(String sessionId);

    boolean existsBySessionIdAndDeletedFalse(String sessionId);
}
