package com.govos.doc.api.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ActivateDocumentVersionRequest(
        @NotNull UUID versionId
) {
}
