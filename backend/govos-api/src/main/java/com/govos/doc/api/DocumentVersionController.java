package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.version.CreateDocumentVersionRequest;
import com.govos.doc.dto.version.DocumentVersionResponse;
import com.govos.doc.dto.version.DocumentVersionSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents/versions")
@Validated
@Tag(name = "Document Versions", description = "Document version lifecycle")
@SecurityRequirement(name = "bearerAuth")
public class DocumentVersionController {

    private static final Logger log = LoggerFactory.getLogger(DocumentVersionController.class);

    private final DocumentApplicationService documentApplicationService;

    public DocumentVersionController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Create version", description = "Requires DOC_WRITE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Version created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<DocumentVersionResponse>> createVersion(
            @Valid @RequestBody CreateDocumentVersionRequest request,
            HttpServletRequest httpRequest) {
        DocumentVersionResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createVersion", null, request.documentId(),
                () -> documentApplicationService.createVersion(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Version created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @GetMapping("/{versionId}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Get version", description = "Requires DOC_READ.")
    public ApiResponse<DocumentVersionResponse> findVersion(
            @PathVariable UUID versionId,
            HttpServletRequest httpRequest) {
        DocumentVersionResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findVersion", null, versionId,
                () -> documentApplicationService.findVersion(versionId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "List document versions", description = "Requires DOC_READ.")
    public ApiResponse<List<DocumentVersionSummaryResponse>> listVersions(
            @Parameter(description = "Document identifier") @PathVariable UUID documentId,
            HttpServletRequest httpRequest) {
        List<DocumentVersionSummaryResponse> versions = DocumentApiLogging.execute(
                log, httpRequest, "listVersions", null, documentId,
                () -> documentApplicationService.listVersions(documentId));
        return ApiResponse.ok(versions, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{versionId}/activate")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Activate version", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentVersionResponse> activateVersion(
            @PathVariable UUID versionId,
            HttpServletRequest httpRequest) {
        DocumentVersionResponse response = DocumentApiLogging.execute(
                log, httpRequest, "activateVersion", null, versionId,
                () -> documentApplicationService.activateVersion(versionId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
