package com.govos.doc.repository;

import com.govos.doc.entity.Document;
import com.govos.doc.enums.DocumentClassification;
import com.govos.doc.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndDeletedFalse(UUID id);

    List<Document> findByOrganizationIdAndDeletedFalse(UUID organizationId);

    Page<Document> findByOrganizationIdAndDeletedFalse(UUID organizationId, Pageable pageable);

    List<Document> findByOrganizationIdAndStatusAndDeletedFalse(UUID organizationId, DocumentStatus status);

    Page<Document> findByOrganizationIdAndStatusAndDeletedFalse(
            UUID organizationId, DocumentStatus status, Pageable pageable);

    List<Document> findByOrganizationIdAndFolder_IdAndDeletedFalse(UUID organizationId, UUID folderId);

    List<Document> findByOrganizationIdAndCategory_IdAndDeletedFalse(UUID organizationId, UUID categoryId);

    Optional<Document> findByOrganizationIdAndDocumentNumberAndDeletedFalse(
            UUID organizationId, String documentNumber);

    boolean existsByOrganizationIdAndDocumentNumberAndDeletedFalse(UUID organizationId, String documentNumber);

    List<Document> findByOwnerIdAndDeletedFalse(UUID ownerId);

    Page<Document> findByOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);

    List<Document> findByClassificationAndDeletedFalse(DocumentClassification classification);

    List<Document> findByTitleContainingIgnoreCaseAndDeletedFalse(String title);

    Page<Document> findByTitleContainingIgnoreCaseAndDeletedFalse(String title, Pageable pageable);

    Page<Document> findByDeletedFalse(Pageable pageable);
}
