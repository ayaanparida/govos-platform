package com.govos.doc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateFolderRequest(
        @NotBlank @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String name,
        UUID parentFolderId,
        @NotNull
        UUID ownerId,
        Boolean active
) {
}
