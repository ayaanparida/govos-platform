package com.govos.wrk.repository;

import com.govos.wrk.entity.WorkflowInstance;
import com.govos.wrk.entity.WorkflowInstanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {

    Optional<WorkflowInstance> findByIdAndDeletedFalse(UUID id);

    List<WorkflowInstance> findByWorkflowVersion_IdAndDeletedFalseOrderByCreatedDateDesc(UUID workflowVersionId);

    List<WorkflowInstance> findByReferenceTypeAndReferenceIdAndDeletedFalseOrderByCreatedDateDesc(
            String referenceType, UUID referenceId);

    List<WorkflowInstance> findByStatusAndDeletedFalseOrderByCreatedDateDesc(WorkflowInstanceStatus status);

    Optional<WorkflowInstance> findByCodeAndDeletedFalse(String code);

    boolean existsByCodeAndDeletedFalse(String code);
}
