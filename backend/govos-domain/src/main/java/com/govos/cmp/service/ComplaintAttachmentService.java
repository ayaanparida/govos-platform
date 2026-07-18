package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintAttachmentCreateRequest;
import com.govos.cmp.dto.ComplaintAttachmentDto;

import java.util.List;
import java.util.UUID;

public interface ComplaintAttachmentService {

    ComplaintAttachmentDto addAttachment(ComplaintAttachmentCreateRequest request);

    List<ComplaintAttachmentDto> listAttachments(UUID complaintId);
}
