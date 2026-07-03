package com.govos.doc.repository;

import com.govos.doc.entity.DocumentTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentTagRepository extends JpaRepository<DocumentTag, UUID> {

    Optional<DocumentTag> findByIdAndDeletedFalse(UUID id);

    Optional<DocumentTag> findByNameAndDeletedFalse(String name);

    List<DocumentTag> findByDeletedFalseOrderByNameAsc();

    boolean existsByNameAndDeletedFalse(String name);
}
