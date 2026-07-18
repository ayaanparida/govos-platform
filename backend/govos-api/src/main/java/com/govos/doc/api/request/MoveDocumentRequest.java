package com.govos.doc.api.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MoveDocumentRequest(
        @NotNull UUID folderId
) {
}
