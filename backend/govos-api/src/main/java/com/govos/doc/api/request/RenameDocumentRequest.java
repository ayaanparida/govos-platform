package com.govos.doc.api.request;

import jakarta.validation.constraints.NotBlank;

public record RenameDocumentRequest(
        @NotBlank String title
) {
}
