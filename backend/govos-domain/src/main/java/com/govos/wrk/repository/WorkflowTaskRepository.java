package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowTask;
import com.govos.wrk.entity.WorkflowTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, UUID> {

    Optional<WorkflowTask> findByIdAndDeletedFalse(UUID id);

    List<WorkflowTask> findByWorkflowInstance_IdAndDeletedFalseOrderByCreatedDateAsc(UUID workflowInstanceId);

    List<WorkflowTask> findByAssignedTo_IdAndDeletedFalseOrderByCreatedDateDesc(UUID assignedToId);

    List<WorkflowTask> findByAssignedRole_IdAndDeletedFalseOrderByCreatedDateDesc(UUID assignedRoleId);

    List<WorkflowTask> findByStatusAndDeletedFalseOrderByCreatedDateDesc(WorkflowTaskStatus status);

    Optional<WorkflowTask> findByWorkflowInstance_IdAndStep_IdAndDeletedFalse(
            UUID workflowInstanceId, UUID stepId);
}
