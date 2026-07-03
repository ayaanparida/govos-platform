package com.govos.doc.repository;

import com.govos.doc.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    Optional<DocumentVersion> findByIdAndDeletedFalse(UUID id);

    List<DocumentVersion> findByDocument_IdAndDeletedFalseOrderByVersionNumberDesc(UUID documentId);

    Optional<DocumentVersion> findByDocument_IdAndVersionNumberAndDeletedFalse(UUID documentId, Integer versionNumber);

    boolean existsByDocument_IdAndVersionNumberAndDeletedFalse(UUID documentId, Integer versionNumber);
}
