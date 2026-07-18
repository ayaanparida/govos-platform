package com.govos.doc.api;

import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.doc.api.support.DocumentApiLogging;
import com.govos.doc.application.DocumentApplicationService;
import com.govos.doc.dto.category.CreateDocumentCategoryRequest;
import com.govos.doc.dto.category.DocumentCategoryResponse;
import com.govos.doc.dto.category.UpdateDocumentCategoryRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/documents/categories")
@Validated
@Tag(name = "Document Categories", description = "Document taxonomy categories")
@SecurityRequirement(name = "bearerAuth")
public class DocumentCategoryController {

    private static final Logger log = LoggerFactory.getLogger(DocumentCategoryController.class);

    private final DocumentApplicationService documentApplicationService;

    public DocumentCategoryController(DocumentApplicationService documentApplicationService) {
        this.documentApplicationService = documentApplicationService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Create category", description = "Requires DOC_WRITE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<DocumentCategoryResponse>> createCategory(
            @Valid @RequestBody CreateDocumentCategoryRequest request,
            HttpServletRequest httpRequest) {
        DocumentCategoryResponse created = DocumentApiLogging.execute(
                log, httpRequest, "createCategory", request.organizationId(), null,
                () -> documentApplicationService.createCategory(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Category created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Update category", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentCategoryResponse> updateCategory(
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateDocumentCategoryRequest request,
            HttpServletRequest httpRequest) {
        DocumentCategoryResponse response = DocumentApiLogging.execute(
                log, httpRequest, "updateCategory", null, categoryId,
                () -> documentApplicationService.updateCategory(categoryId, request));
        return ApiResponse.ok(response, null, RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAuthority('DOC_DELETE')")
    @Operation(summary = "Delete category", description = "Requires DOC_DELETE.")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID categoryId,
            HttpServletRequest httpRequest) {
        DocumentApiLogging.executeVoid(
                log, httpRequest, "deleteCategory", null, categoryId,
                () -> documentApplicationService.deleteCategory(categoryId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{categoryId}/restore")
    @PreAuthorize("hasAuthority('DOC_WRITE')")
    @Operation(summary = "Restore category", description = "Requires DOC_WRITE.")
    public ApiResponse<DocumentCategoryResponse> restoreCategory(
            @PathVariable UUID categoryId,
            HttpServletRequest httpRequest) {
        DocumentCategoryResponse response = DocumentApiLogging.execute(
                log, httpRequest, "restoreCategory", null, categoryId,
                () -> documentApplicationService.restoreCategory(categoryId));
        return ApiResponse.ok(response, "Category restored", RequestContextUtils.resolveRequestId(httpRequest));
    }
}
