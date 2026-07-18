package com.govos.doc.repository;

import com.govos.doc.entity.DocumentVersion;
import com.govos.doc.enums.DocumentVersionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {

    Optional<DocumentVersion> findByIdAndDeletedFalse(UUID id);

    List<DocumentVersion> findByDocument_IdAndDeletedFalse(UUID documentId);

    Page<DocumentVersion> findByDocument_IdAndDeletedFalse(UUID documentId, Pageable pageable);

    List<DocumentVersion> findByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(UUID documentId);

    Optional<DocumentVersion> findByChecksum_ValueAndDeletedFalse(String checksum);

    Optional<DocumentVersion> findByStorageLocation_StorageObjectKeyAndDeletedFalse(String storageObjectKey);

    List<DocumentVersion> findByVersionStatusAndDeletedFalse(DocumentVersionStatus versionStatus);

    Optional<DocumentVersion> findTopByDocument_IdAndDeletedFalseOrderByVersionNumber_ValueDesc(UUID documentId);
}
