package com.govos.api.cmp.request;

import com.govos.cmp.enums.ComplaintCommentType;
import com.govos.cmp.enums.ComplaintVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Add a comment to a complaint")
public record AddCommentRequest(
        @NotBlank
        @Schema(description = "Comment text")
        String commentText,
        @NotNull
        @Schema(description = "Comment visibility", example = "INTERNAL")
        ComplaintVisibility visibility,
        @Schema(description = "Comment type", example = "REMARK")
        ComplaintCommentType commentType,
        @Schema(description = "Active flag override")
        Boolean active
) {
}
