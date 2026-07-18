package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.request.MoveFolderRequest;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.folder.CreateFolderRequest;
import com.govos.doc.dto.folder.FolderResponse;
import com.govos.doc.dto.folder.UpdateFolderRequest;
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
@RequestMapping(ApiConstants.BASE_PATH + "/documents/folders")
@Validated
@Tag(name = "Document Folders", description = "Folder hierarchy management")
@SecurityRequirement(name = "bearerAuth")
public class FolderController {

    private static final Logger log = LoggerFactory.getLogger(FolderController.class);

    private final DocumentApplicationService documentApplicationService;

    public FolderController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Create folder", description = "Requires DOC_WRITE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Folder created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<FolderResponse>> createFolder(
            @Valid @RequestBody CreateFolderRequest request,
            HttpServletRequest httpRequest) {
        FolderResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createFolder", request.organizationId(), null,
                () -> documentApplicationService.createFolder(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Folder created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{folderId}")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Update folder", description = "Requires DOC_WRITE.")
    public ApiResponse<FolderResponse> updateFolder(
            @PathVariable UUID folderId,
            @Valid @RequestBody UpdateFolderRequest request,
            HttpServletRequest httpRequest) {
        FolderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "updateFolder", null, folderId,
                () -> documentApplicationService.updateFolder(folderId, request));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/{folderId}")
    @PreAuthorize("hasAuthority('DOC_READ')")
    @Operation(summary = "Get folder", description = "Requires DOC_READ.")
    public ApiResponse<FolderResponse> findFolder(
            @PathVariable UUID folderId,
            HttpServletRequest httpRequest) {
        FolderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "findFolder", null, folderId,
                () -> documentApplicationService.findFolder(folderId));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/{folderId}")
    @PreAuthorize("hasAuthority('DOC_DELETE')")
    @Operation(summary = "Delete folder", description = "Requires DOC_DELETE.")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable UUID folderId,
            HttpServletRequest httpRequest) {
        DocumentApiLogging.executeVoid(
                log, httpRequest, "deleteFolder", null, folderId,
                () -> documentApplicationService.deleteFolder(folderId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{folderId}/restore")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Restore folder", description = "Requires DOC_WRITE.")
    public ApiResponse<FolderResponse> restoreFolder(
            @PathVariable UUID folderId,
            HttpServletRequest httpRequest) {
        FolderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "restoreFolder", null, folderId,
                () -> documentApplicationService.restoreFolder(folderId));
        return ApiResponse.ok(response, "Folder restored", RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{folderId}/move")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Move folder", description = "Requires DOC_WRITE.")
    public ApiResponse<FolderResponse> moveFolder(
            @PathVariable UUID folderId,
            @Valid @RequestBody MoveFolderRequest request,
            HttpServletRequest httpRequest) {
        FolderResponse response = DocumentApiLogging.execute(
                log, httpRequest, "moveFolder", null, folderId,
                () -> documentApplicationService.moveFolder(folderId, request.parentFolderId(), request.version()));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }
}
