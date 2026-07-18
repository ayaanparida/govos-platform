package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.request.CreateMetadataRequest;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.metadata.DocumentMetadataResponse;
import com.govos.doc.dto.metadata.UpdateDocumentMetadataRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents/metadata")
@Validated
@Tag(name = "Document Metadata", description = "Document metadata and OCR attributes")
@SecurityRequirement(name = "bearerAuth")
public class DocumentMetadataController {

    private static final Logger log = LoggerFactory.getLogger(DocumentMetadataController.class);

    private final DocumentApplicationService documentApplicationService;

    public DocumentMetadataController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Create metadata", description = "Requires DOC_WRITE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Metadata created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<DocumentMetadataResponse>> createMetadata(
            @Valid @RequestBody CreateMetadataRequest request,
            HttpServletRequest httpRequest) {
        DocumentMetadataResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createMetadata", null, request.documentId(),
                () -> documentApplicationService.createMetadata(
                        request.documentId(), request.documentVersionId(), request.metadata()));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Metadata created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{metadataId}")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Update metadata", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentMetadataResponse> updateMetadata(
            @PathVariable UUID metadataId,
            @Valid @RequestBody UpdateDocumentMetadataRequest request,
            HttpServletRequest httpRequest) {
        DocumentMetadataResponse response = DocumentApiLogging.execute(
                log, httpRequest, "updateMetadata", null, metadataId,
                () -> documentApplicationService.updateMetadata(metadataId, request));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/document/{documentId}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Find metadata", description = "Requires DOC_READ.")
    public ApiResponse<DocumentMetadataResponse> findMetadata(
            @PathVariable UUID documentId,
            @Parameter(description = "Optional document version scope")
            @RequestParam(required = false) UUID documentVersionId,
            HttpServletRequest httpRequest) {
        DocumentMetadataResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findMetadata", null, documentId,
                () -> documentApplicationService.findMetadata(documentId, documentVersionId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
