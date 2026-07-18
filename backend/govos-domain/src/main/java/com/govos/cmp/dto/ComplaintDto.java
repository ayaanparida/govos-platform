package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import com.govos.cmp.enums.ComplaintStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ComplaintDto(
        UUID id,
        String code,
        String title,
        String description,
        ComplaintStatus status,
        ComplaintPriority priority,
        ComplaintSource source,
        String channel,
        String categoryKey,
        String subCategoryKey,
        String complaintTypeKey,
        UUID citizenUserId,
        UUID submittedByUserId,
        UUID organizationId,
        UUID departmentId,
        UUID officeId,
        UUID assignedOfficerId,
        UUID resolvedByUserId,
        UUID closedByUserId,
        UUID workflowInstanceId,
        Instant submittedAt,
        Instant closedAt,
        Instant archivedAt,
        String rejectionReasonKey,
        String closureReasonKey,
        Boolean isDuplicate,
        UUID primaryComplaintId,
        UUID mergedIntoComplaintId,
        String stateKey,
        String districtKey,
        String ulbKey,
        String wardKey,
        String villageKey,
        BigDecimal latitude,
        BigDecimal longitude,
        String address,
        String landmark,
        String pincode,
        String geoJson,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
