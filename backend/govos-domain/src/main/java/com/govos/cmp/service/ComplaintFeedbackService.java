package com.govos.cmp.service;

import com.govos.cmp.dto.ComplaintFeedbackCreateRequest;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;

import java.util.UUID;

public interface ComplaintFeedbackService {

    ComplaintFeedbackDto createFeedback(ComplaintFeedbackCreateRequest request);

    ComplaintFeedbackDto updateFeedback(UUID id, ComplaintFeedbackUpdateRequest request);

    ComplaintFeedbackDto getFeedback(UUID complaintId);
}
