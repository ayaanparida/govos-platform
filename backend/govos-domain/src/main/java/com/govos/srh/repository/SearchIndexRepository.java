package com.govos.srh.repository;

import com.govos.srh.entity.SearchIndex;
import com.govos.srh.enums.SearchEngineType;
import com.govos.srh.enums.SearchIndexStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchIndexRepository extends JpaRepository<SearchIndex, UUID> {

    Optional<SearchIndex> findByIdAndDeletedFalse(UUID id);

    Optional<SearchIndex> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);

    List<SearchIndex> findAllByStatusAndDeletedFalse(SearchIndexStatus status);

    List<SearchIndex> findAllByEngineTypeAndDeletedFalse(SearchEngineType engineType);

    List<SearchIndex> findAllByActiveTrueAndDeletedFalse();

    List<SearchIndex> findAllByStatusAndActiveTrueAndDeletedFalse(SearchIndexStatus status);
}
