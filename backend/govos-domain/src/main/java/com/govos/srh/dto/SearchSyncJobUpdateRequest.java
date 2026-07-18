package com.govos.srh.dto;

import com.govos.srh.enums.SearchJobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SearchSyncJobUpdateRequest(
        @Size(max = 100)
        String code,
        @NotBlank @Size(max = 255)
        String jobName,
        @NotNull
        SearchJobType jobType,
        @Size(max = 2000)
        String errorMessage,
        Boolean active,
        @NotNull
        Long version
) {
}
