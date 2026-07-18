package com.govos.doc.repository;

import com.govos.doc.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    Optional<Folder> findByIdAndDeletedFalse(UUID id);

    List<Folder> findByOrganizationIdAndDeletedFalse(UUID organizationId);

    List<Folder> findByParentFolder_IdAndDeletedFalse(UUID parentFolderId);

    Optional<Folder> findByPathMetadata_MaterializedPathAndDeletedFalse(String path);

    boolean existsByOrganizationIdAndPathMetadata_MaterializedPathAndDeletedFalse(
            UUID organizationId, String path);
}
