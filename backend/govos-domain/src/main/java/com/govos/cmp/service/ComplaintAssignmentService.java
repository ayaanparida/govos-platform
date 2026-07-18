package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintAssignmentCreateRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ComplaintAssignmentService {

    ComplaintAssignmentDto createAssignment(ComplaintAssignmentCreateRequest request);

    Optional<ComplaintAssignmentDto> getCurrentAssignment(UUID complaintId);

    List<ComplaintAssignmentDto> listAssignments(UUID complaintId);
}
