package com.govos.api.cmp.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.cmp.dto.ComplaintDto;

import java.time.Instant;
import java.util.UUID;

public record ComplaintNotificationPayload(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        String status,
        String categoryKey,
        String priority,
        UUID citizenUserId,
        UUID assignedOfficerId,
        UUID workflowInstanceId,
        Instant eventTime,
        String rejectionReasonKey
) {

    public static ComplaintNotificationPayload from(ComplaintDto complaint, Instant eventTime) {
        return from(complaint, eventTime, null);
    }

    public static ComplaintNotificationPayload from(
            ComplaintDto complaint,
            Instant eventTime,
            String rejectionReasonKey) {
        return new ComplaintNotificationPayload(
                complaint.id(),
                complaint.code(),
                complaint.organizationId(),
                complaint.status() != null ? complaint.status().name() : null,
                complaint.categoryKey(),
                complaint.priority() != null ? complaint.priority().name() : null,
                complaint.citizenUserId(),
                complaint.assignedOfficerId(),
                complaint.workflowInstanceId(),
                eventTime,
                rejectionReasonKey);
    }

    public String toJson(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new ComplaintNotificationIntegrationException(
                    "Failed to serialize complaint notification payload", ex);
        }
    }
}
