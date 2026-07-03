package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowHistory;
import com.govos.wrk.entity.WorkflowHistoryAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowHistoryRepository extends JpaRepository<WorkflowHistory, UUID> {

    Optional<WorkflowHistory> findByIdAndDeletedFalse(UUID id);

    List<WorkflowHistory> findByWorkflowInstance_IdAndDeletedFalseOrderByPerformedAtDesc(
            UUID workflowInstanceId);

    List<WorkflowHistory> findByWorkflowInstance_IdAndActionAndDeletedFalseOrderByPerformedAtDesc(
            UUID workflowInstanceId, WorkflowHistoryAction action);
}
