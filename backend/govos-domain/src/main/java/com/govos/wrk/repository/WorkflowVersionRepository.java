package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowVersionRepository extends JpaRepository<WorkflowVersion, UUID> {

    Optional<WorkflowVersion> findByIdAndDeletedFalse(UUID id);

    List<WorkflowVersion> findByDefinition_IdAndDeletedFalseOrderByVersionNumberDesc(UUID definitionId);

    Optional<WorkflowVersion> findByDefinition_IdAndVersionNumberAndDeletedFalse(
            UUID definitionId, Integer versionNumber);

    Optional<WorkflowVersion> findByDefinition_IdAndPublishedTrueAndDeletedFalse(UUID definitionId);

    boolean existsByDefinition_IdAndVersionNumberAndDeletedFalse(UUID definitionId, Integer versionNumber);
}
