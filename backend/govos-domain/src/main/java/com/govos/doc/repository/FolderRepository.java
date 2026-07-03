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

    Optional<Folder> findByCodeAndDeletedFalse(String code);

    List<Folder> findByDeletedFalseOrderByNameAsc();

    List<Folder> findByOwner_IdAndDeletedFalseOrderByNameAsc(UUID ownerId);

    List<Folder> findByParentFolder_IdAndDeletedFalseOrderByNameAsc(UUID parentFolderId);

    boolean existsByCodeAndDeletedFalse(String code);
}
