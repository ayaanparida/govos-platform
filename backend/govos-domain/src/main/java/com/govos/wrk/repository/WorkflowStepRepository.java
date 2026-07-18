package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {

    Optional<WorkflowStep> findByIdAndDeletedFalse(UUID id);

    List<WorkflowStep> findByWorkflowVersion_IdAndDeletedFalseOrderBySequenceNumberAsc(UUID workflowVersionId);

    Optional<WorkflowStep> findByWorkflowVersion_IdAndSequenceNumberAndDeletedFalse(
            UUID workflowVersionId, Integer sequenceNumber);

    boolean existsByWorkflowVersion_IdAndSequenceNumberAndDeletedFalse(
            UUID workflowVersionId, Integer sequenceNumber);
}
