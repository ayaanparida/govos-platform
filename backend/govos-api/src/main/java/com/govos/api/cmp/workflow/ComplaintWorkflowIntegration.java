package com.govos.api.cmp.workflow;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.wrk.dto.CreateWorkflowHistoryRequest;
import com.govos.wrk.dto.CreateWorkflowInstanceRequest;
import com.govos.wrk.dto.CreateWorkflowTaskRequest;
import com.govos.wrk.dto.UpdateWorkflowInstanceRequest;
import com.govos.wrk.dto.UpdateWorkflowTaskRequest;
import com.govos.wrk.dto.WorkflowDefinitionDto;
import com.govos.wrk.dto.WorkflowInstanceDto;
import com.govos.wrk.dto.WorkflowStepDto;
import com.govos.wrk.dto.WorkflowTaskDto;
import com.govos.wrk.dto.WorkflowVersionDto;
import com.govos.wrk.entity.WorkflowHistoryAction;
import com.govos.wrk.entity.WorkflowInstanceStatus;
import com.govos.wrk.entity.WorkflowStepType;
import com.govos.wrk.entity.WorkflowTaskStatus;
import com.govos.wrk.exception.WrkException;
import com.govos.wrk.service.WorkflowDefinitionService;
import com.govos.wrk.service.WorkflowHistoryService;
import com.govos.wrk.service.WorkflowInstanceService;
import com.govos.wrk.service.WorkflowStepService;
import com.govos.wrk.service.WorkflowTaskService;
import com.govos.wrk.service.WorkflowVersionService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ComplaintWorkflowIntegration {

    public static final String REFERENCE_TYPE = "COMPLAINT";
    public static final String WORKFLOW_CODE = "CMP_STANDARD";

    private final WorkflowDefinitionService workflowDefinitionService;
    private final WorkflowVersionService workflowVersionService;
    private final WorkflowInstanceService workflowInstanceService;
    private final WorkflowStepService workflowStepService;
    private final WorkflowTaskService workflowTaskService;
    private final WorkflowHistoryService workflowHistoryService;

    public ComplaintWorkflowIntegration(
            WorkflowDefinitionService workflowDefinitionService,
            WorkflowVersionService workflowVersionService,
            WorkflowInstanceService workflowInstanceService,
            WorkflowStepService workflowStepService,
            WorkflowTaskService workflowTaskService,
            WorkflowHistoryService workflowHistoryService) {
        this.workflowDefinitionService = workflowDefinitionService;
        this.workflowVersionService = workflowVersionService;
        this.workflowInstanceService = workflowInstanceService;
        this.workflowStepService = workflowStepService;
        this.workflowTaskService = workflowTaskService;
        this.workflowHistoryService = workflowHistoryService;
    }

    public UUID startWorkflowOnSubmit(ComplaintDto complaint, UUID changedByUserId) {
        return run(() -> {
            WorkflowVersionDto publishedVersion = resolvePublishedVersion();
            Instant now = Instant.now();

            WorkflowInstanceDto instance = workflowInstanceService.create(
                    new CreateWorkflowInstanceRequest(
                            null,
                            publishedVersion.id(),
                            REFERENCE_TYPE,
                            complaint.id(),
                            WorkflowInstanceStatus.RUNNING,
                            now,
                            null,
                            true));

            recordHistory(instance.id(), WorkflowHistoryAction.INSTANCE_STARTED, changedByUserId, now,
                    "Complaint submitted");
            return instance.id();
        });
    }

    public void createTaskOnAssign(
            ComplaintDto complaint,
            ComplaintAssignmentCreateRequest assignmentRequest,
            UUID changedByUserId) {
        runVoid(() -> {
            WorkflowInstanceDto instance = requireWorkflowInstance(complaint);
            WorkflowStepDto assignmentStep = resolveAssignmentStep(instance.workflowVersionId());

            WorkflowTaskDto task = workflowTaskService.create(
                    new CreateWorkflowTaskRequest(
                            null,
                            instance.id(),
                            assignmentRequest.officerUserId(),
                            null,
                            assignmentStep.id(),
                            WorkflowTaskStatus.ASSIGNED,
                            null,
                            null,
                            true));

            Instant now = Instant.now();
            recordHistory(instance.id(), WorkflowHistoryAction.TASK_CREATED, changedByUserId, now,
                    "Assignment task created");
            recordHistory(instance.id(), WorkflowHistoryAction.TASK_ASSIGNED, changedByUserId, now,
                    "Task " + task.id());
        });
    }

    public void moveToApprovalOnResolve(ComplaintDto complaint, UUID changedByUserId) {
        runVoid(() -> {
            WorkflowInstanceDto instance = requireWorkflowInstance(complaint);
            WorkflowTaskDto activeTask = requireActiveTask(instance.id());
            completeTask(activeTask, changedByUserId, "Resolution completed");

            WorkflowStepDto approvalStep = resolveNextUserTaskStep(
                    instance.workflowVersionId(), activeTask.stepId())
                    .orElseThrow(() -> new ComplaintWorkflowIntegrationException(
                            "No approval step configured for workflow version: "
                                    + instance.workflowVersionId()));

            workflowTaskService.create(
                    new CreateWorkflowTaskRequest(
                            null,
                            instance.id(),
                            null,
                            null,
                            approvalStep.id(),
                            WorkflowTaskStatus.PENDING,
                            null,
                            null,
                            true));

            recordHistory(instance.id(), WorkflowHistoryAction.TASK_CREATED, changedByUserId, Instant.now(),
                    "Approval task created after resolution");
        });
    }

    public void completeWorkflowOnClose(ComplaintDto complaint, UUID changedByUserId) {
        runVoid(() -> {
            WorkflowInstanceDto instance = requireWorkflowInstance(complaint);
            findActiveTask(instance.id()).ifPresent(task -> completeTask(task, changedByUserId, "Closed with complaint"));

            Instant now = Instant.now();
            workflowInstanceService.update(
                    instance.id(),
                    new UpdateWorkflowInstanceRequest(
                            instance.code(),
                            instance.referenceType(),
                            instance.referenceId(),
                            WorkflowInstanceStatus.COMPLETED,
                            instance.startedAt(),
                            now,
                            instance.active(),
                            instance.version()));

            recordHistory(instance.id(), WorkflowHistoryAction.INSTANCE_COMPLETED, changedByUserId, now,
                    "Complaint closed");
        });
    }

    public void createFollowUpTaskOnReopen(ComplaintDto complaint, UUID changedByUserId) {
        runVoid(() -> {
            WorkflowInstanceDto instance = requireWorkflowInstance(complaint);
            if (instance.status() == WorkflowInstanceStatus.COMPLETED) {
                workflowInstanceService.update(
                        instance.id(),
                        new UpdateWorkflowInstanceRequest(
                                instance.code(),
                                instance.referenceType(),
                                instance.referenceId(),
                                WorkflowInstanceStatus.RUNNING,
                                instance.startedAt(),
                                null,
                                instance.active(),
                                instance.version()));
            }

            WorkflowStepDto followUpStep = resolveAssignmentStep(instance.workflowVersionId());
            workflowTaskService.create(
                    new CreateWorkflowTaskRequest(
                            null,
                            instance.id(),
                            complaint.assignedOfficerId(),
                            null,
                            followUpStep.id(),
                            complaint.assignedOfficerId() != null
                                    ? WorkflowTaskStatus.ASSIGNED
                                    : WorkflowTaskStatus.PENDING,
                            null,
                            null,
                            true));

            recordHistory(instance.id(), WorkflowHistoryAction.TASK_CREATED, changedByUserId, Instant.now(),
                    "Follow-up task created after reopen");
        });
    }

    public void reassignCurrentTask(ComplaintDto complaint, UUID changedByUserId) {
        runVoid(() -> {
            WorkflowInstanceDto instance = requireWorkflowInstance(complaint);
            WorkflowTaskDto activeTask = requireActiveTask(instance.id());

            workflowTaskService.update(
                    activeTask.id(),
                    new UpdateWorkflowTaskRequest(
                            activeTask.code(),
                            null,
                            null,
                            WorkflowTaskStatus.PENDING,
                            activeTask.dueDate(),
                            null,
                            activeTask.active(),
                            activeTask.version()));

            recordHistory(instance.id(), WorkflowHistoryAction.TASK_ASSIGNED, changedByUserId, Instant.now(),
                    "Task reassignment requested for task " + activeTask.id());
        });
    }

    private WorkflowVersionDto resolvePublishedVersion() {
        WorkflowDefinitionDto definition = workflowDefinitionService.getByCode(WORKFLOW_CODE);
        return workflowVersionService.getPublishedByDefinitionId(definition.id());
    }

    private WorkflowInstanceDto requireWorkflowInstance(ComplaintDto complaint) {
        if (complaint.workflowInstanceId() != null) {
            return workflowInstanceService.getById(complaint.workflowInstanceId());
        }

        List<WorkflowInstanceDto> instances =
                workflowInstanceService.getByReference(REFERENCE_TYPE, complaint.id());
        return instances.stream()
                .filter(instance -> instance.status() != WorkflowInstanceStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new ComplaintWorkflowIntegrationException(
                        "No workflow instance found for complaint: " + complaint.id()));
    }

    private WorkflowStepDto resolveAssignmentStep(UUID workflowVersionId) {
        return resolveUserTaskSteps(workflowVersionId).stream()
                .findFirst()
                .orElseThrow(() -> new ComplaintWorkflowIntegrationException(
                        "No user task steps configured for workflow version: " + workflowVersionId));
    }

    private Optional<WorkflowStepDto> resolveNextUserTaskStep(UUID workflowVersionId, UUID currentStepId) {
        List<WorkflowStepDto> steps = resolveUserTaskSteps(workflowVersionId);
        boolean foundCurrent = false;
        for (WorkflowStepDto step : steps) {
            if (foundCurrent) {
                return Optional.of(step);
            }
            if (step.id().equals(currentStepId)) {
                foundCurrent = true;
            }
        }
        return Optional.empty();
    }

    private List<WorkflowStepDto> resolveUserTaskSteps(UUID workflowVersionId) {
        return workflowStepService.getByWorkflowVersionId(workflowVersionId).stream()
                .filter(step -> step.stepType() == WorkflowStepType.USER_TASK)
                .sorted(Comparator.comparing(WorkflowStepDto::sequenceNumber))
                .toList();
    }

    private Optional<WorkflowTaskDto> findActiveTask(UUID workflowInstanceId) {
        return workflowTaskService.getByWorkflowInstanceId(workflowInstanceId).stream()
                .filter(task -> task.status() != WorkflowTaskStatus.COMPLETED
                        && task.status() != WorkflowTaskStatus.CANCELLED)
                .findFirst();
    }

    private WorkflowTaskDto requireActiveTask(UUID workflowInstanceId) {
        return findActiveTask(workflowInstanceId)
                .orElseThrow(() -> new ComplaintWorkflowIntegrationException(
                        "No active workflow task found for instance: " + workflowInstanceId));
    }

    private void completeTask(WorkflowTaskDto task, UUID changedByUserId, String remarks) {
        workflowTaskService.update(
                task.id(),
                new UpdateWorkflowTaskRequest(
                        task.code(),
                        task.assignedToId(),
                        task.assignedRoleId(),
                        WorkflowTaskStatus.COMPLETED,
                        task.dueDate(),
                        Instant.now(),
                        task.active(),
                        task.version()));

        recordHistory(task.workflowInstanceId(), WorkflowHistoryAction.TASK_COMPLETED, changedByUserId,
                Instant.now(), remarks);
    }

    private void recordHistory(
            UUID workflowInstanceId,
            WorkflowHistoryAction action,
            UUID performedById,
            Instant performedAt,
            String remarks) {
        workflowHistoryService.create(
                new CreateWorkflowHistoryRequest(
                        null,
                        workflowInstanceId,
                        action,
                        performedById,
                        performedAt,
                        remarks,
                        true));
    }

    private void runVoid(Runnable action) {
        try {
            action.run();
        } catch (ComplaintWorkflowIntegrationException ex) {
            throw ex;
        } catch (WrkException ex) {
            throw new ComplaintWorkflowIntegrationException(
                    "Workflow integration failed: " + ex.getMessage(), ex);
        }
    }

    private <T> T run(WorkflowAction<T> action) {
        try {
            return action.execute();
        } catch (ComplaintWorkflowIntegrationException ex) {
            throw ex;
        } catch (WrkException ex) {
            throw new ComplaintWorkflowIntegrationException(
                    "Workflow integration failed: " + ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    private interface WorkflowAction<T> {
        T execute();
    }
}
