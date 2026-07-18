package com.govos.doc.repository;

import com.govos.doc.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentMetadataRepository extends JpaRepository<DocumentMetadata, UUID> {

    Optional<DocumentMetadata> findByIdAndDeletedFalse(UUID id);

    List<DocumentMetadata> findByDocument_IdAndDeletedFalse(UUID documentId);

    List<DocumentMetadata> findByDocumentVersion_IdAndDeletedFalse(UUID documentVersionId);
}
