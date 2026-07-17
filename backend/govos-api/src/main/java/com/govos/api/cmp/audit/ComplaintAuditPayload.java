package com.govos.api.cmp.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.cmp.dto.ComplaintDto;

import java.time.Instant;
import java.util.UUID;

public record ComplaintAuditPayload(
        String auditAction,
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID workflowInstanceId,
        String status,
        String categoryKey,
        String priority,
        UUID performedByUserId,
        String requestId,
        Instant eventTime
) {

    public static ComplaintAuditPayload from(
            ComplaintAuditAction action,
            ComplaintDto complaint,
            UUID performedByUserId,
            String requestId,
            Instant eventTime) {
        return new ComplaintAuditPayload(
                action.eventCode(),
                complaint.id(),
                complaint.code(),
                complaint.organizationId(),
                complaint.workflowInstanceId(),
                complaint.status() != null ? complaint.status().name() : null,
                complaint.categoryKey(),
                complaint.priority() != null ? complaint.priority().name() : null,
                performedByUserId,
                requestId,
                eventTime);
    }

    public String toJson(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new ComplaintAuditIntegrationException(
                    "Failed to serialize complaint audit payload", ex);
        }
    }
}
