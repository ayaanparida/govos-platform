package com.govos.api.cmp.request;

import com.govos.cmp.enums.ComplaintAttachmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Link a document attachment to a complaint")
public record AddAttachmentRequest(
        @NotNull
        @Schema(description = "Document identifier from DOC module")
        UUID documentId,
        @Schema(description = "Optional document version identifier")
        UUID documentVersionId,
        @NotNull
        @Schema(description = "Attachment type", example = "DOCUMENT")
        ComplaintAttachmentType attachmentType,
        @Size(max = 255)
        @Schema(description = "Display name for the attachment")
        String displayName,
        @Schema(description = "Sort order within the complaint")
        Integer sortOrder,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
