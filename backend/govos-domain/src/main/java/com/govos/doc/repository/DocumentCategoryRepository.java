package com.govos.doc.repository;

import com.govos.doc.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, UUID> {

    Optional<DocumentCategory> findByIdAndDeletedFalse(UUID id);

    List<DocumentCategory> findByOrganizationIdAndDeletedFalse(UUID organizationId);

    Optional<DocumentCategory> findByCodeAndDeletedFalse(String code);

    Optional<DocumentCategory> findByOrganizationIdAndCodeAndDeletedFalse(UUID organizationId, String code);

    List<DocumentCategory> findByParentCategory_IdAndDeletedFalse(UUID parentCategoryId);

    boolean existsByOrganizationIdAndCodeAndDeletedFalse(UUID organizationId, String code);
}
