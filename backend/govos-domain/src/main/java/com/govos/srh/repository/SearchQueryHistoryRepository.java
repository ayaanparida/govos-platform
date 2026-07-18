package com.govos.srh.repository;

import com.govos.srh.entity.SearchQueryHistory;
import com.govos.srh.enums.SearchQueryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchQueryHistoryRepository extends JpaRepository<SearchQueryHistory, UUID> {

    List<SearchQueryHistory> findAllByOrganizationIdAndDeletedFalse(UUID organizationId);

    List<SearchQueryHistory> findAllByUserIdAndDeletedFalse(UUID userId);

    List<SearchQueryHistory> findAllByQueryTypeAndDeletedFalse(SearchQueryType queryType);

    List<SearchQueryHistory> findAllByOrganizationIdAndUserIdAndDeletedFalse(
            UUID organizationId, UUID userId);

    List<SearchQueryHistory> findAllByCreatedDateBetweenAndDeletedFalse(Instant from, Instant to);
}
