package com.govos.doc.api;

import com.govos.api.common.pagination.SortParser;
import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.api.common.validation.PaginationRequest;
import com.govos.doc.api.request.ActivateDocumentVersionRequest;
import com.govos.doc.api.request.ChangeClassificationRequest;
import com.govos.doc.api.request.MoveDocumentRequest;
import com.govos.doc.api.request.RenameDocumentRequest;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.document.CreateDocumentRequest;
import com.govos.doc.dto.document.DocumentListResponse;
import com.govos.doc.dto.document.DocumentResponse;
import com.govos.doc.dto.document.DocumentSearchResponse;
import com.govos.doc.dto.document.UpdateDocumentRequest;
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
import org.springframework.data.domain.PageRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents")
@Validated
@Tag(name = "Document Management", description = "Document CRUD, lifecycle, and listing")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentApplicationService documentApplicationService;

    public DocumentController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Create document", description = "Creates a new document. Requires DOC_WRITE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @Valid @RequestBody CreateDocumentRequest request,
            HttpServletRequest httpRequest) {
        DocumentResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createDocument", request.organizationId(), null,
                () -> documentApplicationService.createDocument(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Document created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{documentId}")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Update document", description = "Updates an active document. Requires DOC_WRITE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<DocumentResponse> updateDocument(
            @Parameter(description = "Document identifier") @PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentRequest request,
            HttpServletRequest httpRequest) {
        DocumentResponse updated = DocumentApiLogging.execute(
                log, httpRequest, "updateDocument", null, documentId,
                () -> documentApplicationService.updateDocument(documentId, request));
        return ApiResponse.ok(updated, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Get document", description = "Returns a document by id. Requires DOC_READ.")
    public ApiResponse<DocumentResponse> findDocument(
            @PathVariable UUID documentId,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findDocument", null, documentId,
                () -> documentApplicationService.findDocument(documentId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/by-number/{documentNumber}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Get document by number", description = "Requires DOC_READ.")
    public ApiResponse<DocumentResponse> findDocumentByNumber(
            @PathVariable String documentNumber,
            @Parameter(description = "Organization identifier") @RequestParam @NotNull UUID organizationId,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findDocumentByNumber", organizationId, null,
                () -> documentApplicationService.findDocumentByNumber(organizationId, documentNumber));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "List documents", description = "Paginated organization document list. Requires DOC_READ.")
    public ApiResponse<DocumentListResponse> listDocuments(
            @Parameter(description = "Organization identifier") @RequestParam @NotNull UUID organizationId,
            @Valid PaginationRequest pagination,
            HttpServletRequest httpRequest) {
        var pageable = PageRequest.of(
                pagination.page(), pagination.size(), SortParser.parse(pagination.sort()));
        DocumentListResponse response = DocumentApiLogging.execute(
                log, httpRequest, "listDocuments", organizationId, null,
                () -> documentApplicationService.listDocuments(organizationId, pageable));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Search documents", description = "Organization-scoped document search. Requires DOC_READ.")
    public ApiResponse<List<DocumentSearchResponse>> searchDocuments(
            @RequestParam @NotNull UUID organizationId,
            @RequestParam(required = false) String query,
            @Valid PaginationRequest pagination,
            HttpServletRequest httpRequest) {
        var pageable = PageRequest.of(
                pagination.page(), pagination.size(), SortParser.parse(pagination.sort()));
        List<DocumentSearchResponse> results = DocumentApiLogging.execute(
                log, httpRequest, "searchDocuments", organizationId, null,
                () -> documentApplicationService.searchDocuments(organizationId, query, pageable));
        return ApiResponse.ok(results, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasAuthority('DOC_DELETE')")
    @Operation(summary = "Delete document", description = "Soft-deletes a document. Requires DOC_DELETE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Document deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteDocument(
            @PathVariable UUID documentId,
            HttpServletRequest httpRequest) {
        DocumentApiLogging.executeVoid(
                log, httpRequest, "deleteDocument", null, documentId,
                () -> documentApplicationService.deleteDocument(documentId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{documentId}/restore")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Restore document", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentResponse> restoreDocument(
            @PathVariable UUID documentId,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "restoreDocument", null, documentId,
                () -> documentApplicationService.restoreDocument(documentId));
        return ApiResponse.ok(response, "Document restored", RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{documentId}/archive")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Archive document", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentResponse> archiveDocument(
            @PathVariable UUID documentId,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "archiveDocument", null, documentId,
                () -> documentApplicationService.archiveDocument(documentId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{documentId}/rename")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Rename document", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentResponse> renameDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody RenameDocumentRequest request,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "renameDocument", null, documentId,
                () -> documentApplicationService.renameDocument(documentId, request.title()));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{documentId}/move")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Move document", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentResponse> moveDocument(
            @PathVariable UUID documentId,
            @Valid @RequestBody MoveDocumentRequest request,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "moveDocument", null, documentId,
                () -> documentApplicationService.moveDocument(documentId, request.folderId()));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{documentId}/classification")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Change classification", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentResponse> changeClassification(
            @PathVariable UUID documentId,
            @Valid @RequestBody ChangeClassificationRequest request,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "changeClassification", null, documentId,
                () -> documentApplicationService.changeClassification(documentId, request.classification()));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{documentId}/activate-version")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Activate document version", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentResponse> activateVersion(
            @PathVariable UUID documentId,
            @Valid @RequestBody ActivateDocumentVersionRequest request,
            HttpServletRequest httpRequest) {
        DocumentResponse response = DocumentApiLogging.execute(
                log, httpRequest, "activateDocumentVersion", null, documentId,
                () -> documentApplicationService.activateVersion(documentId, request.versionId()));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
