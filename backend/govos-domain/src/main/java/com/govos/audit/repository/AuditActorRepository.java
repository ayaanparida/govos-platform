package com.govos.audit.repository;

import com.govos.audit.entity.AuditActor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditActorRepository extends JpaRepository<AuditActor, UUID> {

    Optional<AuditActor> findByIdAndDeletedFalse(UUID id);

    Optional<AuditActor> findByUser_IdAndDeletedFalse(UUID userId);

    List<AuditActor> findByDeletedFalseOrderByDisplayNameAsc();
}
