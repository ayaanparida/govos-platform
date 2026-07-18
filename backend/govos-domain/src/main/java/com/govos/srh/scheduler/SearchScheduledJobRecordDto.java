package com.govos.srh.scheduler;

import java.time.Instant;
import java.util.UUID;

public record SearchScheduledJobRecordDto(
        UUID recordId,
        String jobName,
        String status,
        Instant startedAt,
        Instant completedAt,
        long durationMs,
        String errorMessage,
        long documentsProcessed,
        int retryCount
) {

    public static SearchScheduledJobRecordDto from(SearchScheduledJobRecord record) {
        return new SearchScheduledJobRecordDto(
                record.getRecordId(),
                record.getJobName(),
                record.getStatus().name(),
                record.getStartedAt(),
                record.getCompletedAt(),
                record.getDurationMs(),
                record.getErrorMessage(),
                record.getDocumentsProcessed(),
                record.getRetryCount());
    }
}
