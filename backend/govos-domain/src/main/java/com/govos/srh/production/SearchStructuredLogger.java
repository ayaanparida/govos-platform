package com.govos.srh.production;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SearchStructuredLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.govos.srh.operation");

    public void logOperation(SearchOperationContext context) {
        LOGGER.info(
                "search_operation operation={} status={} durationMs={} organizationId={} requestId={} indexCode={} entityType={} referenceId={}",
                safe(context.operation()),
                safe(context.status()),
                context.durationMs(),
                safeUuid(context.organizationId()),
                safe(context.requestId()),
                safe(context.indexCode()),
                safe(context.entityType()),
                safeUuid(context.referenceId()));
    }

    private static String safe(String value) {
        return value != null ? value : "-";
    }

    private static String safeUuid(UUID value) {
        return value != null ? value.toString() : "-";
    }
}
