package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ComplaintService {

    ComplaintDto create(ComplaintCreateRequest request);

    ComplaintDto update(UUID id, ComplaintUpdateRequest request);

    ComplaintDto getById(UUID id);

    ComplaintDto getByCode(String code);

    Page<ComplaintDto> list(Pageable pageable);

    void softDelete(UUID id);

    ComplaintDto restore(UUID id);

    ComplaintDto submit(UUID id, UUID changedByUserId);

    ComplaintDto accept(UUID id, UUID changedByUserId);

    ComplaintDto assign(UUID id, ComplaintAssignmentCreateRequest assignmentRequest);

    ComplaintDto startProgress(UUID id, UUID changedByUserId);

    ComplaintDto resolve(UUID id, UUID changedByUserId, String reasonKey);

    ComplaintDto close(UUID id, UUID changedByUserId, String closureReasonKey);

    ComplaintDto archive(UUID id, UUID changedByUserId);

    ComplaintDto reopen(UUID id, UUID changedByUserId, String reasonKey);

    ComplaintDto reject(UUID id, UUID changedByUserId, String rejectionReasonKey);

    ComplaintDto requestReassignment(UUID id, UUID changedByUserId, String rejectionReasonKey);

    ComplaintDto markDuplicate(UUID id, ComplaintDuplicateCreateRequest duplicateRequest, UUID changedByUserId);

    ComplaintDto merge(UUID survivingComplaintId, ComplaintMergeCreateRequest mergeRequest, UUID changedByUserId);

    ComplaintDto linkWorkflowInstance(UUID complaintId, UUID workflowInstanceId);
}
