package com.govos.doc.api.request;

import com.govos.doc.enums.DocumentClassification;
import jakarta.validation.constraints.NotNull;

public record ChangeClassificationRequest(
        @NotNull DocumentClassification classification
) {
}
