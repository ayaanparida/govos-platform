package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.storage.CreateStorageProviderRequest;
import com.govos.doc.dto.storage.StorageProviderResponse;
import com.govos.doc.dto.storage.UpdateStorageProviderRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents/storage-providers")
@Validated
@Tag(name = "Document Storage Providers", description = "Storage provider registry administration")
@SecurityRequirement(name = "bearerAuth")
public class StorageProviderController {

    private static final Logger log = LoggerFactory.getLogger(StorageProviderController.class);

    private final DocumentApplicationService documentApplicationService;

    public StorageProviderController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Create storage provider", description = "Requires DOC_ADMIN.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Provider created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<StorageProviderResponse>> createStorageProvider(
            @Valid @RequestBody CreateStorageProviderRequest request,
            HttpServletRequest httpRequest) {
        StorageProviderResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createStorageProvider", null, null,
                () -> documentApplicationService.createStorageProvider(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Storage provider created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{providerId}")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Update storage provider", description = "Requires DOC_ADMIN.")
    public ApiResponse<StorageProviderResponse> updateStorageProvider(
            @PathVariable UUID providerId,
            @Valid @RequestBody UpdateStorageProviderRequest request,
            HttpServletRequest httpRequest) {
        StorageProviderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "updateStorageProvider", null, providerId,
                () -> documentApplicationService.updateStorageProvider(providerId, request));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/{providerId}")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Get storage provider", description = "Requires DOC_ADMIN.")
    public ApiResponse<StorageProviderResponse> findStorageProvider(
            @PathVariable UUID providerId,
            HttpServletRequest httpRequest) {
        StorageProviderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findStorageProvider", null, providerId,
                () -> documentApplicationService.findStorageProvider(providerId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{providerId}/activate")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Activate storage provider", description = "Requires DOC_ADMIN.")
    public ApiResponse<StorageProviderResponse> activateStorageProvider(
            @PathVariable UUID providerId,
            HttpServletRequest httpRequest) {
        StorageProviderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "activateStorageProvider", null, providerId,
                () -> documentApplicationService.activateStorageProvider(providerId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{providerId}/deactivate")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Deactivate storage provider", description = "Requires DOC_ADMIN.")
    public ApiResponse<StorageProviderResponse> deactivateStorageProvider(
            @PathVariable UUID providerId,
            HttpServletRequest httpRequest) {
        StorageProviderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "deactivateStorageProvider", null, providerId,
                () -> documentApplicationService.deactivateStorageProvider(providerId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{providerId}/default")
    @PreAuthorize("hasAuthority('DOC_ADMIN')")
    @Operation(summary = "Set default storage provider", description = "Requires DOC_ADMIN.")
    public ApiResponse<StorageProviderResponse> setDefaultStorageProvider(
            @PathVariable UUID providerId,
            HttpServletRequest httpRequest) {
        StorageProviderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "setDefaultStorageProvider", null, providerId,
                () -> documentApplicationService.setDefaultStorageProvider(providerId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
