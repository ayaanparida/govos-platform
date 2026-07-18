package com.govos.srh.repository;

import com.govos.srh.entity.SearchAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchAliasRepository extends JpaRepository<SearchAlias, UUID> {

    Optional<SearchAlias> findByAliasNameAndDeletedFalse(String aliasName);

    @Query("SELECT a FROM SearchAlias a WHERE a.searchIndex.id = :searchIndexId AND a.deleted = false")
    List<SearchAlias> findAllBySearchIndexIdAndDeletedFalse(@Param("searchIndexId") UUID searchIndexId);

    List<SearchAlias> findAllByActiveAliasTrueAndDeletedFalse();
}
