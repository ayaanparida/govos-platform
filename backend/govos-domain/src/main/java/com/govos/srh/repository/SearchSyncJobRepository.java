package com.govos.srh.repository;

import com.govos.srh.entity.SearchSyncJob;
import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SearchSyncJobRepository extends JpaRepository<SearchSyncJob, UUID> {

    Optional<SearchSyncJob> findByIdAndDeletedFalse(UUID id);

    @Query("SELECT j FROM SearchSyncJob j WHERE j.searchIndex.id = :searchIndexId AND j.deleted = false")
    List<SearchSyncJob> findAllBySearchIndexIdAndDeletedFalse(@Param("searchIndexId") UUID searchIndexId);

    @Query("SELECT j FROM SearchSyncJob j WHERE j.status = :jobStatus AND j.deleted = false")
    List<SearchSyncJob> findAllByJobStatusAndDeletedFalse(@Param("jobStatus") SearchJobStatus jobStatus);

    List<SearchSyncJob> findAllByJobTypeAndDeletedFalse(SearchJobType jobType);

    List<SearchSyncJob> findAllByStartedAtBeforeAndDeletedFalse(Instant startedAt);
}
