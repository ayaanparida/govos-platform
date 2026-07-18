package com.govos.srh.dto;

import com.govos.srh.enums.SearchJobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SearchSyncJobCreateRequest(
        @Size(max = 100)
        String code,
        @NotNull
        UUID searchIndexId,
        @NotBlank @Size(max = 255)
        String jobName,
        @NotNull
        SearchJobType jobType,
        Boolean active
) {
}
