package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintMergeCreateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;

import java.util.List;
import java.util.UUID;

public interface ComplaintMergeService {

    ComplaintMergeDto createMerge(ComplaintMergeCreateRequest request);

    List<ComplaintMergeDto> listMerges(UUID survivingComplaintId);
}
