package com.govos.cmp.dto;

import com.govos.cmp.enums.ComplaintPriority;
import com.govos.cmp.enums.ComplaintSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ComplaintCreateRequest(
        @NotBlank @Size(max = 500)
        String title,
        String description,
        ComplaintPriority priority,
        @NotNull
        ComplaintSource source,
        @Size(max = 100)
        String channel,
        @Size(max = 100)
        String categoryKey,
        @Size(max = 100)
        String subCategoryKey,
        @Size(max = 100)
        String complaintTypeKey,
        UUID citizenUserId,
        UUID submittedByUserId,
        UUID organizationId,
        UUID departmentId,
        UUID officeId,
        @Size(max = 100)
        String stateKey,
        @Size(max = 100)
        String districtKey,
        @Size(max = 100)
        String ulbKey,
        @Size(max = 100)
        String wardKey,
        @Size(max = 100)
        String villageKey,
        @DecimalMin("-90") @DecimalMax("90")
        BigDecimal latitude,
        @DecimalMin("-180") @DecimalMax("180")
        BigDecimal longitude,
        @Size(max = 1000)
        String address,
        @Size(max = 255)
        String landmark,
        @Size(max = 20)
        String pincode,
        String geoJson,
        Boolean active
) {
}
