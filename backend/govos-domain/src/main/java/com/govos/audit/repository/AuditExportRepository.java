package com.govos.audit.repository;

import com.govos.audit.entity.AuditExport;
import com.govos.audit.entity.AuditExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditExportRepository extends JpaRepository<AuditExport, UUID> {

    Optional<AuditExport> findByIdAndDeletedFalse(UUID id);

    List<AuditExport> findByRequestedBy_IdAndDeletedFalseOrderByRequestedTimeDesc(UUID requestedById);

    List<AuditExport> findByStatusAndDeletedFalseOrderByRequestedTimeDesc(AuditExportStatus status);
}
