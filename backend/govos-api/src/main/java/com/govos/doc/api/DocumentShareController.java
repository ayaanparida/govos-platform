package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.share.CreateShareRequest;
import com.govos.doc.dto.share.ShareResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents/shares")
@Validated
@Tag(name = "Document Shares", description = "Document sharing and access grants")
@SecurityRequirement(name = "bearerAuth")
public class DocumentShareController {

    private static final Logger log = LoggerFactory.getLogger(DocumentShareController.class);

    private final DocumentApplicationService documentApplicationService;

    public DocumentShareController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_SHARE')")
    @Operation(summary = "Create share", description = "Requires DOC_SHARE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Share created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ShareResponse>> createShare(
            @Valid @RequestBody CreateShareRequest request,
            HttpServletRequest httpRequest) {
        ShareResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createShare", null, request.documentId(),
                () -> documentApplicationService.createShare(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Share created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @GetMapping("/{shareId}")
    @PreAuthorize("hasAuthority('DOC_SHARE')")
    @Operation(summary = "Get share", description = "Requires DOC_SHARE.")
    public ApiResponse<ShareResponse> findShare(
            @PathVariable UUID shareId,
            HttpServletRequest httpRequest) {
        ShareResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findShare", null, shareId,
                () -> documentApplicationService.findShare(shareId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{shareId}/expire")
    @PreAuthorize("hasAuthority('DOC_SHARE')")
    @Operation(summary = "Expire share", description = "Requires DOC_SHARE.")
    public ApiResponse<ShareResponse> expireShare(
            @PathVariable UUID shareId,
            HttpServletRequest httpRequest) {
        ShareResponse response = DocumentApiLogging.execute(
                log, httpRequest, "expireShare", null, shareId,
                () -> documentApplicationService.expireShare(shareId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{shareId}/revoke")
    @PreAuthorize("hasAuthority('DOC_SHARE')")
    @Operation(summary = "Revoke share", description = "Requires DOC_SHARE.")
    public ApiResponse<ShareResponse> revokeShare(
            @PathVariable UUID shareId,
            HttpServletRequest httpRequest) {
        ShareResponse response = DocumentApiLogging.execute(
                log, httpRequest, "revokeShare", null, shareId,
                () -> documentApplicationService.revokeShare(shareId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
