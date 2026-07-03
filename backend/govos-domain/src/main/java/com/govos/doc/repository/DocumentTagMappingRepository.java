package com.govos.doc.repository;

import com.govos.doc.entity.DocumentTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentTagMappingRepository extends JpaRepository<DocumentTagMapping, UUID> {

    Optional<DocumentTagMapping> findByIdAndDeletedFalse(UUID id);

    List<DocumentTagMapping> findByDocument_IdAndDeletedFalse(UUID documentId);

    List<DocumentTagMapping> findByTag_IdAndDeletedFalse(UUID tagId);

    boolean existsByDocument_IdAndTag_IdAndDeletedFalse(UUID documentId, UUID tagId);
}
