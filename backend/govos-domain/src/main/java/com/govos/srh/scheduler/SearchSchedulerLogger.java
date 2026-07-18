package com.govos.srh.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SearchSchedulerLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.govos.srh.scheduler");

    public void logExecution(
            String jobName,
            String status,
            long durationMs,
            long documentsProcessed,
            String errorCode) {
        LOGGER.info(
                "scheduler_execution jobName={} status={} durationMs={} documentsProcessed={} errorCode={}",
                safe(jobName),
                safe(status),
                durationMs,
                documentsProcessed,
                safe(errorCode));
    }

    private static String safe(String value) {
        return value != null ? value : "-";
    }
}
