package com.govos.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDocumentTagRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 100)
        String name,
        @Size(max = 500)
        String description,
        Boolean active,
        Long version
) {
}
