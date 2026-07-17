package com.govos.api.cmp.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record ComplaintSearchDocument(
        UUID complaintId,
        String complaintCode,
        UUID organizationId,
        UUID workflowInstanceId,
        String title,
        String description,
        String status,
        String priority,
        String categoryKey,
        String subCategoryKey,
        UUID citizenUserId,
        UUID assignedOfficerId,
        UUID departmentId,
        UUID officeId,
        String source,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant createdDate,
        Instant updatedDate,
        String searchText,
        Long searchVersion,
        Boolean active,
        Boolean deleted
) {

    public static ComplaintSearchDocument from(ComplaintDto complaint) {
        return from(complaint, complaint.active(), false);
    }

    public static ComplaintSearchDocument from(ComplaintDto complaint, boolean active, boolean deleted) {
        return new ComplaintSearchDocument(
                complaint.id(),
                complaint.code(),
                complaint.organizationId(),
                complaint.workflowInstanceId(),
                complaint.title(),
                complaint.description(),
                complaint.status() != null ? complaint.status().name() : null,
                complaint.priority() != null ? complaint.priority().name() : null,
                complaint.categoryKey(),
                complaint.subCategoryKey(),
                complaint.citizenUserId(),
                complaint.assignedOfficerId(),
                complaint.departmentId(),
                complaint.officeId(),
                complaint.source() != null ? complaint.source().name() : null,
                complaint.latitude(),
                complaint.longitude(),
                complaint.createdDate(),
                complaint.updatedDate(),
                buildSearchText(complaint),
                complaint.version(),
                active,
                deleted);
    }

    public static ComplaintSearchDocument archived(ComplaintDto complaint) {
        return from(complaint, false, false);
    }

    public String toJson(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new ComplaintSearchIntegrationException(
                    "Failed to serialize complaint search document", ex);
        }
    }

    private static String buildSearchText(ComplaintDto complaint) {
        return Stream.of(
                        complaint.code(),
                        complaint.title(),
                        complaint.description(),
                        complaint.categoryKey(),
                        complaint.subCategoryKey(),
                        enumName(complaint.status()),
                        enumName(complaint.priority()),
                        enumName(complaint.source()),
                        complaint.address(),
                        complaint.landmark(),
                        complaint.wardKey(),
                        complaint.villageKey())
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
    }

    private static String enumName(ComplaintStatus status) {
        return status != null ? status.name() : null;
    }

    private static String enumName(ComplaintPriority priority) {
        return priority != null ? priority.name() : null;
    }

    private static String enumName(ComplaintSource source) {
        return source != null ? source.name() : null;
    }
}
