package com.govos.doc.repository;

import com.govos.doc.entity.Document;
import com.govos.doc.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndDeletedFalse(UUID id);

    Optional<Document> findByCodeAndDeletedFalse(String code);

    List<Document> findByDeletedFalseOrderByOriginalFilenameAsc();

    List<Document> findByFolder_IdAndDeletedFalseOrderByOriginalFilenameAsc(UUID folderId);

    List<Document> findByOwner_IdAndDeletedFalseOrderByOriginalFilenameAsc(UUID ownerId);

    List<Document> findByStatusAndDeletedFalseOrderByOriginalFilenameAsc(DocumentStatus status);

    boolean existsByCodeAndDeletedFalse(String code);
}
