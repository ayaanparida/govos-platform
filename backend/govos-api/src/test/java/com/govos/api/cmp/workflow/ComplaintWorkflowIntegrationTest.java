package com.govos.api.cmp.workflow;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintAssignmentType;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;
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
import com.govos.wrk.exception.WorkflowDefinitionNotFoundException;
import com.govos.wrk.service.WorkflowDefinitionService;
import com.govos.wrk.service.WorkflowHistoryService;
import com.govos.wrk.service.WorkflowInstanceService;
import com.govos.wrk.service.WorkflowStepService;
import com.govos.wrk.service.WorkflowTaskService;
import com.govos.wrk.service.WorkflowVersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComplaintWorkflowIntegrationTest {

    @Mock private WorkflowDefinitionService workflowDefinitionService;
    @Mock private WorkflowVersionService workflowVersionService;
    @Mock private WorkflowInstanceService workflowInstanceService;
    @Mock private WorkflowStepService workflowStepService;
    @Mock private WorkflowTaskService workflowTaskService;
    @Mock private WorkflowHistoryService workflowHistoryService;

    private ComplaintWorkflowIntegration integration;

    private static final UUID COMPLAINT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID OFFICER_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID DEFINITION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID VERSION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INSTANCE_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID RESOLUTION_STEP_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final UUID APPROVAL_STEP_ID = UUID.fromString("77777777-7777-7777-7777-777777777777");
    private static final UUID TASK_ID = UUID.fromString("88888888-8888-8888-8888-888888888888");

    @BeforeEach
    void setUp() {
        integration = new ComplaintWorkflowIntegration(
                workflowDefinitionService,
                workflowVersionService,
                workflowInstanceService,
                workflowStepService,
                workflowTaskService,
                workflowHistoryService);
    }

    @Test
    void shouldStartWorkflowOnSubmit() {
        stubPublishedWorkflow();
        when(workflowInstanceService.create(any(CreateWorkflowInstanceRequest.class)))
                .thenReturn(sampleInstance(WorkflowInstanceStatus.RUNNING, null));

        UUID instanceId = integration.startWorkflowOnSubmit(sampleComplaint(null, ComplaintStatus.SUBMITTED), USER_ID);

        assertThat(instanceId).isEqualTo(INSTANCE_ID);
        verify(workflowDefinitionService).getByCode(ComplaintWorkflowIntegration.WORKFLOW_CODE);

        ArgumentCaptor<CreateWorkflowInstanceRequest> captor = ArgumentCaptor.forClass(CreateWorkflowInstanceRequest.class);
        verify(workflowInstanceService).create(captor.capture());
        CreateWorkflowInstanceRequest request = captor.getValue();
        assertThat(request.referenceType()).isEqualTo(ComplaintWorkflowIntegration.REFERENCE_TYPE);
        assertThat(request.referenceId()).isEqualTo(COMPLAINT_ID);
        assertThat(request.status()).isEqualTo(WorkflowInstanceStatus.RUNNING);

        verify(workflowHistoryService).create(any(CreateWorkflowHistoryRequest.class));
    }

    @Test
    void shouldCreateTaskOnAssign() {
        ComplaintDto complaint = sampleComplaint(INSTANCE_ID, ComplaintStatus.ASSIGNED);
        ComplaintAssignmentCreateRequest assignmentRequest = new ComplaintAssignmentCreateRequest(
                COMPLAINT_ID, ComplaintAssignmentType.INITIAL, null, null, OFFICER_ID, USER_ID, null, true);

        when(workflowInstanceService.getById(INSTANCE_ID)).thenReturn(sampleInstance(WorkflowInstanceStatus.RUNNING, null));
        when(workflowStepService.getByWorkflowVersionId(VERSION_ID)).thenReturn(sampleSteps());
        when(workflowTaskService.create(any(CreateWorkflowTaskRequest.class)))
                .thenReturn(sampleTask(WorkflowTaskStatus.ASSIGNED, RESOLUTION_STEP_ID));

        integration.createTaskOnAssign(complaint, assignmentRequest, USER_ID);

        ArgumentCaptor<CreateWorkflowTaskRequest> captor = ArgumentCaptor.forClass(CreateWorkflowTaskRequest.class);
        verify(workflowTaskService).create(captor.capture());
        CreateWorkflowTaskRequest request = captor.getValue();
        assertThat(request.workflowInstanceId()).isEqualTo(INSTANCE_ID);
        assertThat(request.assignedToId()).isEqualTo(OFFICER_ID);
        assertThat(request.stepId()).isEqualTo(RESOLUTION_STEP_ID);
        assertThat(request.status()).isEqualTo(WorkflowTaskStatus.ASSIGNED);
    }

    @Test
    void shouldMoveToApprovalStepOnResolve() {
        ComplaintDto complaint = sampleComplaint(INSTANCE_ID, ComplaintStatus.RESOLVED);
        WorkflowTaskDto activeTask = sampleTask(WorkflowTaskStatus.IN_PROGRESS, RESOLUTION_STEP_ID);

        when(workflowInstanceService.getById(INSTANCE_ID)).thenReturn(sampleInstance(WorkflowInstanceStatus.RUNNING, null));
        when(workflowTaskService.getByWorkflowInstanceId(INSTANCE_ID)).thenReturn(List.of(activeTask));
        when(workflowStepService.getByWorkflowVersionId(VERSION_ID)).thenReturn(sampleSteps());
        when(workflowTaskService.update(eq(TASK_ID), any(UpdateWorkflowTaskRequest.class)))
                .thenReturn(sampleTask(WorkflowTaskStatus.COMPLETED, RESOLUTION_STEP_ID));

        integration.moveToApprovalOnResolve(complaint, USER_ID);

        verify(workflowTaskService).update(eq(TASK_ID), any(UpdateWorkflowTaskRequest.class));
        verify(workflowTaskService).create(any(CreateWorkflowTaskRequest.class));
    }

    @Test
    void shouldCompleteWorkflowOnClose() {
        ComplaintDto complaint = sampleComplaint(INSTANCE_ID, ComplaintStatus.CLOSED);
        WorkflowTaskDto activeTask = sampleTask(WorkflowTaskStatus.IN_PROGRESS, RESOLUTION_STEP_ID);

        when(workflowInstanceService.getById(INSTANCE_ID)).thenReturn(sampleInstance(WorkflowInstanceStatus.RUNNING, null));
        when(workflowTaskService.getByWorkflowInstanceId(INSTANCE_ID)).thenReturn(List.of(activeTask));
        when(workflowTaskService.update(eq(TASK_ID), any(UpdateWorkflowTaskRequest.class)))
                .thenReturn(sampleTask(WorkflowTaskStatus.COMPLETED, RESOLUTION_STEP_ID));
        when(workflowInstanceService.update(eq(INSTANCE_ID), any(UpdateWorkflowInstanceRequest.class)))
                .thenReturn(sampleInstance(WorkflowInstanceStatus.COMPLETED, Instant.now()));

        integration.completeWorkflowOnClose(complaint, USER_ID);

        ArgumentCaptor<UpdateWorkflowInstanceRequest> captor = ArgumentCaptor.forClass(UpdateWorkflowInstanceRequest.class);
        verify(workflowInstanceService).update(eq(INSTANCE_ID), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(WorkflowInstanceStatus.COMPLETED);
        assertThat(captor.getValue().completedAt()).isNotNull();
    }

    @Test
    void shouldCreateFollowUpTaskOnReopen() {
        ComplaintDto complaint = withAssignedOfficer(
                sampleComplaint(INSTANCE_ID, ComplaintStatus.REOPENED), OFFICER_ID);

        when(workflowInstanceService.getById(INSTANCE_ID))
                .thenReturn(sampleInstance(WorkflowInstanceStatus.COMPLETED, Instant.now()));
        when(workflowInstanceService.update(eq(INSTANCE_ID), any(UpdateWorkflowInstanceRequest.class)))
                .thenReturn(sampleInstance(WorkflowInstanceStatus.RUNNING, null));
        when(workflowStepService.getByWorkflowVersionId(VERSION_ID)).thenReturn(sampleSteps());

        integration.createFollowUpTaskOnReopen(complaint, USER_ID);

        verify(workflowInstanceService).update(eq(INSTANCE_ID), any(UpdateWorkflowInstanceRequest.class));
        verify(workflowTaskService).create(any(CreateWorkflowTaskRequest.class));
    }

    @Test
    void shouldReassignCurrentTaskOnReassignmentRequest() {
        ComplaintDto complaint = sampleComplaint(INSTANCE_ID, ComplaintStatus.PENDING_REASSIGNMENT);
        WorkflowTaskDto activeTask = sampleTask(WorkflowTaskStatus.ASSIGNED, RESOLUTION_STEP_ID);

        when(workflowInstanceService.getById(INSTANCE_ID)).thenReturn(sampleInstance(WorkflowInstanceStatus.RUNNING, null));
        when(workflowTaskService.getByWorkflowInstanceId(INSTANCE_ID)).thenReturn(List.of(activeTask));
        when(workflowTaskService.update(eq(TASK_ID), any(UpdateWorkflowTaskRequest.class)))
                .thenReturn(sampleTask(WorkflowTaskStatus.PENDING, RESOLUTION_STEP_ID));

        integration.reassignCurrentTask(complaint, USER_ID);

        ArgumentCaptor<UpdateWorkflowTaskRequest> captor = ArgumentCaptor.forClass(UpdateWorkflowTaskRequest.class);
        verify(workflowTaskService).update(eq(TASK_ID), captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(WorkflowTaskStatus.PENDING);
        assertThat(captor.getValue().assignedToId()).isNull();
    }

    @Test
    void shouldWrapWorkflowDefinitionNotFoundAsIntegrationException() {
        when(workflowDefinitionService.getByCode(ComplaintWorkflowIntegration.WORKFLOW_CODE))
                .thenThrow(new WorkflowDefinitionNotFoundException(ComplaintWorkflowIntegration.WORKFLOW_CODE));

        assertThatThrownBy(() -> integration.startWorkflowOnSubmit(
                sampleComplaint(null, ComplaintStatus.SUBMITTED), USER_ID))
                .isInstanceOf(ComplaintWorkflowIntegrationException.class)
                .hasMessageContaining("Workflow integration failed");
    }

    @Test
    void shouldFailWhenNoWorkflowInstanceExistsForComplaint() {
        ComplaintDto complaint = sampleComplaint(null, ComplaintStatus.ASSIGNED);
        when(workflowInstanceService.getByReference(ComplaintWorkflowIntegration.REFERENCE_TYPE, COMPLAINT_ID))
                .thenReturn(List.of());

        assertThatThrownBy(() -> integration.createTaskOnAssign(
                complaint,
                new ComplaintAssignmentCreateRequest(
                        COMPLAINT_ID, ComplaintAssignmentType.INITIAL, null, null, OFFICER_ID, USER_ID, null, true),
                USER_ID))
                .isInstanceOf(ComplaintWorkflowIntegrationException.class)
                .hasMessageContaining("No workflow instance found");
    }

    private void stubPublishedWorkflow() {
        when(workflowDefinitionService.getByCode(ComplaintWorkflowIntegration.WORKFLOW_CODE))
                .thenReturn(new WorkflowDefinitionDto(
                        DEFINITION_ID, ComplaintWorkflowIntegration.WORKFLOW_CODE, "Standard", null, true, 0L,
                        null, null, null, null));
        when(workflowVersionService.getPublishedByDefinitionId(DEFINITION_ID))
                .thenReturn(new WorkflowVersionDto(
                        VERSION_ID, "v1", DEFINITION_ID, 1, true, true, 0L, null, null, null, null));
    }

    private static List<WorkflowStepDto> sampleSteps() {
        return List.of(
                new WorkflowStepDto(
                        RESOLUTION_STEP_ID, "RESOLVE", VERSION_ID, "Resolve", WorkflowStepType.USER_TASK,
                        1, 24, true, 0L, null, null, null, null),
                new WorkflowStepDto(
                        APPROVAL_STEP_ID, "APPROVE", VERSION_ID, "Approve", WorkflowStepType.USER_TASK,
                        2, 24, true, 0L, null, null, null, null));
    }

    private static WorkflowInstanceDto sampleInstance(WorkflowInstanceStatus status, Instant completedAt) {
        return new WorkflowInstanceDto(
                INSTANCE_ID, "WFI-COMPLAINT", VERSION_ID, ComplaintWorkflowIntegration.REFERENCE_TYPE, COMPLAINT_ID,
                status, Instant.now(), completedAt, true, 0L, null, null, null, null);
    }

    private static WorkflowTaskDto sampleTask(WorkflowTaskStatus status, UUID stepId) {
        return new WorkflowTaskDto(
                TASK_ID, "WFT-1", INSTANCE_ID, OFFICER_ID, null, stepId, status,
                null, null, true, 0L, null, null, null, null);
    }

    private static ComplaintDto sampleComplaint(UUID workflowInstanceId, ComplaintStatus status) {
        return new ComplaintDto(
                COMPLAINT_ID, "CMP-2026-0001", "Water leak", "Description",
                status, ComplaintPriority.MEDIUM, ComplaintSource.CITIZEN_PORTAL,
                "WEB", "WATER_SUPPLY", "PIPE_LEAK", "SERVICE_REQUEST",
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                null, null, null, null, null, workflowInstanceId,
                null, null, null, null, null, false,
                null, null, "KA", "BLR", null, null, null,
                new BigDecimal("12.9716000"), new BigDecimal("77.5946000"),
                "Main Street", null, "560001", null,
                true, 0L, null, null, null, null);
    }

    private ComplaintDto withAssignedOfficer(ComplaintDto complaint, UUID officerId) {
        return new ComplaintDto(
                complaint.id(), complaint.code(), complaint.title(), complaint.description(),
                complaint.status(), complaint.priority(), complaint.source(),
                complaint.channel(), complaint.categoryKey(), complaint.subCategoryKey(), complaint.complaintTypeKey(),
                complaint.citizenUserId(), complaint.submittedByUserId(), complaint.organizationId(),
                complaint.departmentId(), complaint.officeId(), officerId,
                complaint.resolvedByUserId(), complaint.closedByUserId(), complaint.workflowInstanceId(),
                complaint.submittedAt(), complaint.closedAt(), complaint.archivedAt(),
                complaint.rejectionReasonKey(), complaint.closureReasonKey(), complaint.isDuplicate(),
                complaint.primaryComplaintId(), complaint.mergedIntoComplaintId(),
                complaint.stateKey(), complaint.districtKey(), complaint.ulbKey(), complaint.wardKey(),
                complaint.villageKey(), complaint.latitude(), complaint.longitude(),
                complaint.address(), complaint.landmark(), complaint.pincode(), complaint.geoJson(),
                complaint.active(), complaint.version(), complaint.createdBy(), complaint.createdDate(),
                complaint.updatedBy(), complaint.updatedDate());
    }
}
