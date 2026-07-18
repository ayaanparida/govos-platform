package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintCommentCreateRequest;
import com.govos.cmp.dto.ComplaintCommentDto;

import java.util.List;
import java.util.UUID;

public interface ComplaintCommentService {

    ComplaintCommentDto addComment(ComplaintCommentCreateRequest request);

    List<ComplaintCommentDto> listComments(UUID complaintId);
}
