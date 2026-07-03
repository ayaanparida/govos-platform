package com.govos.doc.repository;

import com.govos.doc.entity.DocumentAccessLog;
import com.govos.doc.entity.DocumentAccessAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentAccessLogRepository extends JpaRepository<DocumentAccessLog, UUID> {

    Optional<DocumentAccessLog> findByIdAndDeletedFalse(UUID id);

    List<DocumentAccessLog> findByDocument_IdAndDeletedFalseOrderByAccessedAtDesc(UUID documentId);

    List<DocumentAccessLog> findByUser_IdAndDeletedFalseOrderByAccessedAtDesc(UUID userId);

    List<DocumentAccessLog> findByDocument_IdAndActionAndDeletedFalseOrderByAccessedAtDesc(
            UUID documentId, DocumentAccessAction action);
}
