package com.govos.srh.dto;

import com.govos.srh.enums.SearchJobStatus;
import com.govos.srh.enums.SearchJobType;

import java.time.Instant;
import java.util.UUID;

public record SearchSyncJobDto(
        UUID id,
        String code,
        UUID searchIndexId,
        String jobName,
        SearchJobType jobType,
        SearchJobStatus jobStatus,
        Instant startedAt,
        Instant completedAt,
        Long processedCount,
        Long successCount,
        Long failureCount,
        String errorMessage,
        Boolean active,
        Long version,
        String createdBy,
        Instant createdDate,
        String updatedBy,
        Instant updatedDate
) {
}
