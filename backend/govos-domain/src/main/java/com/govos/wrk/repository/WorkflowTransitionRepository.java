package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {

    Optional<WorkflowTransition> findByIdAndDeletedFalse(UUID id);

    List<WorkflowTransition> findByFromStep_IdAndDeletedFalse(UUID fromStepId);

    List<WorkflowTransition> findByToStep_IdAndDeletedFalse(UUID toStepId);

    Optional<WorkflowTransition> findByFromStep_IdAndToStep_IdAndDeletedFalse(
            UUID fromStepId, UUID toStepId);

    boolean existsByFromStep_IdAndToStep_IdAndDeletedFalse(UUID fromStepId, UUID toStepId);
}
