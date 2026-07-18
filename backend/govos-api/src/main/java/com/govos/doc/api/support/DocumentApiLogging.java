package com.govos.doc.api.support;

import com.govos.api.common.util.RequestContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

public final class DocumentApiLogging {

    private DocumentApiLogging() {
    }

    public static <T> T execute(
            Logger log,
            HttpServletRequest request,
            String operation,
            UUID organizationId,
            UUID resourceId,
            Supplier<T> action) {
        long startNanos = System.nanoTime();
        try {
            return action.get();
        } finally {
            log.info(
                    "doc_api requestId={} operation={} organizationId={} resourceId={} durationMs={}",
                    RequestContextUtils.resolveRequestId(request),
                    operation,
                    organizationId,
                    resourceId,
                    (System.nanoTime() - startNanos) / 1_000_000L);
        }
    }

    public static void executeVoid(
            Logger log,
            HttpServletRequest request,
            String operation,
            UUID organizationId,
            UUID resourceId,
            Runnable action) {
        execute(log, request, operation, organizationId, resourceId, () -> {
            action.run();
            return null;
        });
    }
}
