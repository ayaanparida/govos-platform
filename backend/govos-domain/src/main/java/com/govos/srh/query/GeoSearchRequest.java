package com.govos.srh.query;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record GeoSearchRequest(
        @NotBlank
        String indexCode,
        @NotNull
        UUID organizationId,
        UUID userId,
        @NotNull
        BigDecimal latitude,
        @NotNull
        BigDecimal longitude,
        Double radiusKm,
        BigDecimal topLeftLatitude,
        BigDecimal topLeftLongitude,
        BigDecimal bottomRightLatitude,
        BigDecimal bottomRightLongitude,
        String queryText,
        @Valid
        SearchFilters filters,
        @Valid
        SearchPage page,
        Boolean sortByDistance
) {
}
