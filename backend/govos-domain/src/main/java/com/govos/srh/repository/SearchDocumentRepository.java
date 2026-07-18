package com.govos.srh.repository;

import com.govos.srh.entity.SearchDocument;
import com.govos.srh.enums.SearchDocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchDocumentRepository extends JpaRepository<SearchDocument, UUID> {

    Optional<SearchDocument> findByIdAndDeletedFalse(UUID id);

    Optional<SearchDocument> findByReferenceIdAndDeletedFalse(UUID referenceId);

    @Query("SELECT d FROM SearchDocument d WHERE d.searchIndex.id = :searchIndexId AND d.deleted = false")
    List<SearchDocument> findAllBySearchIndexIdAndDeletedFalse(@Param("searchIndexId") UUID searchIndexId);

    List<SearchDocument> findAllByOrganizationIdAndDeletedFalse(UUID organizationId);

    List<SearchDocument> findAllByEntityTypeAndDeletedFalse(String entityType);

    @Query("SELECT d FROM SearchDocument d WHERE d.status = :documentStatus AND d.deleted = false")
    List<SearchDocument> findAllByDocumentStatusAndDeletedFalse(
            @Param("documentStatus") SearchDocumentStatus documentStatus);

    List<SearchDocument> findAllByReferenceIdAndOrganizationIdAndDeletedFalse(
            UUID referenceId, UUID organizationId);

    List<SearchDocument> findAllByLastIndexedAtBeforeAndDeletedFalse(Instant lastIndexedAt);
}
