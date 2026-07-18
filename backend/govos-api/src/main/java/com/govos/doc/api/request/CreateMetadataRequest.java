package com.govos.doc.api.request;

import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMetadataRequest(
        @NotNull UUID documentId,
        UUID documentVersionId,
        @NotNull @Valid UpdateDocumentMetadataRequest metadata
) {
}
