package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintDuplicateCreateRequest;
import com.govos.cmp.dto.ComplaintDuplicateDto;

import java.util.List;
import java.util.UUID;

public interface ComplaintDuplicateService {

    ComplaintDuplicateDto createDuplicate(ComplaintDuplicateCreateRequest request);

    List<ComplaintDuplicateDto> listDuplicates(UUID primaryComplaintId);
}
