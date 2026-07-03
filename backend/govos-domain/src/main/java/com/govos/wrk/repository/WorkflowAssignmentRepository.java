package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowAssignmentRepository extends JpaRepository<WorkflowAssignment, UUID> {

    Optional<WorkflowAssignment> findByIdAndDeletedFalse(UUID id);

    List<WorkflowAssignment> findByWorkflowTask_IdAndDeletedFalse(UUID workflowTaskId);

    List<WorkflowAssignment> findByUser_IdAndDeletedFalse(UUID userId);

    Optional<WorkflowAssignment> findByWorkflowTask_IdAndUser_IdAndDeletedFalse(
            UUID workflowTaskId, UUID userId);

    boolean existsByWorkflowTask_IdAndUser_IdAndDeletedFalse(UUID workflowTaskId, UUID userId);
}
