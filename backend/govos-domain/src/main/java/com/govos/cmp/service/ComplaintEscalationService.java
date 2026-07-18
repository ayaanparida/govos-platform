package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintEscalationCreateRequest;
import com.govos.cmp.dto.ComplaintEscalationDto;

import java.util.List;
import java.util.UUID;

public interface ComplaintEscalationService {

    ComplaintEscalationDto createEscalation(ComplaintEscalationCreateRequest request);

    List<ComplaintEscalationDto> listEscalations(UUID complaintId);
}
