package com.govos.doc.repository;

import com.govos.doc.entity.DocumentAccessLog;
import com.govos.doc.enums.AccessOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, UUID> {

    Optional<DocumentAccessLog> findByIdAndDeletedFalse(UUID id);

    List<DocumentAccessLog> findByDocument_IdAndDeletedFalse(UUID documentId);

    List<DocumentAccessLog> findByUserIdAndDeletedFalse(UUID userId);

    List<DocumentAccessLog> findByOperationAndDeletedFalse(AccessOperation operation);

    List<DocumentAccessLog> findByAccessedAtBetweenAndDeletedFalse(Instant start, Instant end);
}
