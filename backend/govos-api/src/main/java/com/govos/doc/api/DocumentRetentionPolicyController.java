package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.retention.CreateRetentionPolicyRequest;
import com.govos.doc.dto.retention.RetentionPolicyResponse;
import com.govos.doc.dto.retention.UpdateRetentionPolicyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents/retention")
@Validated
@Tag(name = "Document Retention", description = "Retention policy administration")
@SecurityRequirement(name = "bearerAuth")
public class DocumentRetentionPolicyController {

    private static final Logger log = LoggerFactory.getLogger(DocumentRetentionPolicyController.class);

    private final DocumentApplicationService documentApplicationService;

    public DocumentRetentionPolicyController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Create retention policy", description = "Requires DOC_ADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Policy created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<RetentionPolicyResponse>> createRetentionPolicy(
            @Valid @RequestBody CreateRetentionPolicyRequest request,
            HttpServletRequest httpRequest) {
        RetentionPolicyResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createRetentionPolicy", request.organizationId(), null,
                () -> documentApplicationService.createRetentionPolicy(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Retention policy created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{policyId}")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Update retention policy", description = "Requires DOC_ADMIN.")
    public ApiResponse<RetentionPolicyResponse> updateRetentionPolicy(
            @PathVariable UUID policyId,
            @Valid @RequestBody UpdateRetentionPolicyRequest request,
            HttpServletRequest httpRequest) {
        RetentionPolicyResponse response = DocumentApiLogging.execute(
                log, httpRequest, "updateRetentionPolicy", null, policyId,
                () -> documentApplicationService.updateRetentionPolicy(policyId, request));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/{policyId}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Get retention policy", description = "Requires DOC_READ.")
    public ApiResponse<RetentionPolicyResponse> findRetentionPolicy(
            @PathVariable UUID policyId,
            HttpServletRequest httpRequest) {
        RetentionPolicyResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findRetentionPolicy", null, policyId,
                () -> documentApplicationService.findRetentionPolicy(policyId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/{policyId}")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Delete retention policy", description = "Requires DOC_ADMIN.")
    public ResponseEntity<Void> deleteRetentionPolicy(
            @PathVariable UUID policyId,
            HttpServletRequest httpRequest) {
        DocumentApiLogging.executeVoid(
                log, httpRequest, "deleteRetentionPolicy", null, policyId,
                () -> documentApplicationService.deleteRetentionPolicy(policyId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{policyId}/restore")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Restore retention policy", description = "Requires DOC_ADMIN.")
    public ApiResponse<RetentionPolicyResponse> restoreRetentionPolicy(
            @PathVariable UUID policyId,
            HttpServletRequest httpRequest) {
        RetentionPolicyResponse response = DocumentApiLogging.execute(
                log, httpRequest, "restoreRetentionPolicy", null, policyId,
                () -> documentApplicationService.restoreRetentionPolicy(policyId));
        return ApiResponse.ok(response, "Retention policy restored", RequestContextUtils.resolveRequestId(httpRequest));
    }
}
