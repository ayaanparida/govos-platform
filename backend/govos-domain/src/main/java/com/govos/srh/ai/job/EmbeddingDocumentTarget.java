package com.govos.srh.ai.job;

import java.util.UUID;

public record EmbeddingDocumentTarget(
        UUID referenceId,
        UUID organizationId,
        String entityType,
        String text
) {
}
