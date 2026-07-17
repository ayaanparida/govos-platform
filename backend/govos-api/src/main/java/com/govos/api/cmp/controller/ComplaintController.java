package com.govos.api.cmp.controller;

import com.govos.api.cmp.application.ComplaintApplicationService;
import com.govos.api.cmp.mapper.ComplaintApiMapper;
import com.govos.api.cmp.request.AddAttachmentRequest;
import com.govos.api.cmp.request.AddCommentRequest;
import com.govos.api.cmp.request.AssignComplaintRequest;
import com.govos.api.cmp.request.CloseComplaintRequest;
import com.govos.api.cmp.request.CreateEscalationRequest;
import com.govos.api.cmp.request.CreateFeedbackRequest;
import com.govos.api.cmp.request.MarkDuplicateRequest;
import com.govos.api.cmp.request.MergeComplaintRequest;
import com.govos.api.cmp.request.RejectComplaintRequest;
import com.govos.api.cmp.request.ReopenComplaintRequest;
import com.govos.api.cmp.request.RequestReassignmentRequest;
import com.govos.api.cmp.request.ResolveComplaintRequest;
import com.govos.api.common.pagination.PageMapper;
import com.govos.api.common.pagination.PageResponse;
import com.govos.api.common.pagination.SortParser;
import com.govos.api.common.response.ApiResponse;
import com.govos.api.common.response.ErrorResponse;
import com.govos.api.common.util.ApiConstants;
import com.govos.api.common.util.RequestContextUtils;
import com.govos.api.common.validation.PaginationRequest;
import com.govos.cmp.dto.ComplaintAssignmentDto;
import com.govos.cmp.dto.ComplaintAttachmentDto;
import com.govos.cmp.dto.ComplaintCommentDto;
import com.govos.cmp.dto.ComplaintCreateRequest;
import com.govos.cmp.dto.ComplaintDto;
import com.govos.cmp.dto.ComplaintDuplicateDto;
import com.govos.cmp.dto.ComplaintEscalationDto;
import com.govos.cmp.dto.ComplaintFeedbackDto;
import com.govos.cmp.dto.ComplaintFeedbackUpdateRequest;
import com.govos.cmp.dto.ComplaintMergeDto;
import com.govos.cmp.dto.ComplaintUpdateRequest;
import com.govos.security.annotation.CurrentUser;
import com.govos.security.jwt.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/complaints")
@Validated
@Tag(name = "Complaint Management", description = "Complaint CRUD, lifecycle, and child resources")
@SecurityRequirement(name = "bearerAuth")
public class ComplaintController {

    private final ComplaintApplicationService complaintApplicationService;
    private final ComplaintApiMapper complaintApiMapper;

    public ComplaintController(
            ComplaintApplicationService complaintApplicationService,
            ComplaintApiMapper complaintApiMapper) {
        this.complaintApplicationService = complaintApplicationService;
        this.complaintApiMapper = complaintApiMapper;
    }

    // --- CRUD ---

    @PostMapping
    @Operation(summary = "Create complaint", description = "Create a new complaint draft.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Complaint created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ApiResponse<ComplaintDto>> create(
            @Valid @RequestBody ComplaintCreateRequest request,
            HttpServletRequest httpRequest) {
        ComplaintDto created = complaintApplicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, "Complaint created", RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update complaint", description = "Update an active complaint.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Complaint updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Business validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ApiResponse<ComplaintDto> update(
            @Parameter(description = "Complaint identifier") @PathVariable UUID id,
            @Valid @RequestBody ComplaintUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.update(id, request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint by id")
    public ApiResponse<ComplaintDto> getById(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.getById(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get complaint by code")
    public ApiResponse<ComplaintDto> getByCode(
            @PathVariable String code,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.getByCode(code),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping
    @Operation(summary = "List complaints", description = "Paginated list of non-deleted complaints.")
    public ApiResponse<PageResponse<ComplaintDto>> list(
            @Valid PaginationRequest pagination,
            HttpServletRequest httpRequest) {
        var pageable = PageRequest.of(
                pagination.page(),
                pagination.size(),
                SortParser.parse(pagination.sort()));
        return ApiResponse.ok(
                PageMapper.toPageResponse(complaintApplicationService.list(pageable)),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete complaint")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Complaint deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Complaint not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @PathVariable UUID id) {
        complaintApplicationService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore soft-deleted complaint")
    public ApiResponse<ComplaintDto> restore(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.restore(id),
                "Complaint restored",
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Lifecycle ---

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit complaint")
    public ApiResponse<ComplaintDto> submit(
            @PathVariable UUID id,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.submit(id, currentUser.getUserId()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Accept complaint at intake")
    public ApiResponse<ComplaintDto> accept(
            @PathVariable UUID id,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.accept(id, currentUser.getUserId()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject complaint at intake")
    public ApiResponse<ComplaintDto> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectComplaintRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.reject(id, currentUser.getUserId(), request.rejectionReasonKey()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign complaint to officer")
    public ApiResponse<ComplaintDto> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignComplaintRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.assign(
                        id, complaintApiMapper.toAssignmentCreateRequest(id, request, currentUser)),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/start-progress")
    @Operation(summary = "Start complaint progress")
    public ApiResponse<ComplaintDto> startProgress(
            @PathVariable UUID id,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.startProgress(id, currentUser.getUserId()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve complaint")
    public ApiResponse<ComplaintDto> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody ResolveComplaintRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.resolve(id, currentUser.getUserId(), request.reasonKey()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/close")
    @Operation(summary = "Close complaint")
    public ApiResponse<ComplaintDto> close(
            @PathVariable UUID id,
            @Valid @RequestBody CloseComplaintRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.close(id, currentUser.getUserId(), request.closureReasonKey()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive closed complaint")
    public ApiResponse<ComplaintDto> archive(
            @PathVariable UUID id,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.archive(id, currentUser.getUserId()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/reopen")
    @Operation(summary = "Reopen closed complaint")
    public ApiResponse<ComplaintDto> reopen(
            @PathVariable UUID id,
            @Valid @RequestBody ReopenComplaintRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.reopen(id, currentUser.getUserId(), request.reasonKey()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/request-reassignment")
    @Operation(summary = "Request complaint reassignment")
    public ApiResponse<ComplaintDto> requestReassignment(
            @PathVariable UUID id,
            @Valid @RequestBody RequestReassignmentRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.requestReassignment(
                        id, currentUser.getUserId(), request.rejectionReasonKey()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/{id}/duplicate")
    @Operation(summary = "Mark complaint as duplicate")
    public ApiResponse<ComplaintDto> markDuplicate(
            @PathVariable UUID id,
            @Valid @RequestBody MarkDuplicateRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.markDuplicate(
                        id,
                        complaintApiMapper.toDuplicateCreateRequest(id, request, currentUser),
                        currentUser.getUserId()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @PostMapping("/merge")
    @Operation(summary = "Merge complaints")
    public ApiResponse<ComplaintDto> merge(
            @Valid @RequestBody MergeComplaintRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.merge(
                        request.survivingComplaintId(),
                        complaintApiMapper.toMergeCreateRequest(request, currentUser),
                        currentUser.getUserId()),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Comments ---

    @PostMapping("/{id}/comments")
    @Operation(summary = "Add complaint comment")
    public ResponseEntity<ApiResponse<ComplaintCommentDto>> addComment(
            @PathVariable UUID id,
            @Valid @RequestBody AddCommentRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        ComplaintCommentDto created = complaintApplicationService.addComment(
                complaintApiMapper.toCommentCreateRequest(id, request, currentUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, null, RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "List complaint comments")
    public ApiResponse<List<ComplaintCommentDto>> listComments(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.listComments(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Attachments ---

    @PostMapping("/{id}/attachments")
    @Operation(summary = "Add complaint attachment")
    public ResponseEntity<ApiResponse<ComplaintAttachmentDto>> addAttachment(
            @PathVariable UUID id,
            @Valid @RequestBody AddAttachmentRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        ComplaintAttachmentDto created = complaintApplicationService.addAttachment(
                complaintApiMapper.toAttachmentCreateRequest(id, request, currentUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, null, RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @GetMapping("/{id}/attachments")
    @Operation(summary = "List complaint attachments")
    public ApiResponse<List<ComplaintAttachmentDto>> listAttachments(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.listAttachments(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Feedback ---

    @PostMapping("/{id}/feedback")
    @Operation(summary = "Submit complaint feedback")
    public ResponseEntity<ApiResponse<ComplaintFeedbackDto>> createFeedback(
            @PathVariable UUID id,
            @Valid @RequestBody CreateFeedbackRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        ComplaintFeedbackDto created = complaintApplicationService.createFeedback(
                complaintApiMapper.toFeedbackCreateRequest(id, request, currentUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, null, RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @PutMapping("/{id}/feedback")
    @Operation(summary = "Update complaint feedback")
    public ApiResponse<ComplaintFeedbackDto> updateFeedback(
            @PathVariable UUID id,
            @Valid @RequestBody ComplaintFeedbackUpdateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.updateFeedback(id, request),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    @GetMapping("/{id}/feedback")
    @Operation(summary = "Get complaint feedback")
    public ApiResponse<ComplaintFeedbackDto> getFeedback(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.getFeedback(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Assignments ---

    @GetMapping("/{id}/assignments")
    @Operation(summary = "List complaint assignments")
    public ApiResponse<List<ComplaintAssignmentDto>> listAssignments(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.listAssignments(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Escalations ---

    @PostMapping("/{id}/escalations")
    @Operation(summary = "Create complaint escalation")
    public ResponseEntity<ApiResponse<ComplaintEscalationDto>> createEscalation(
            @PathVariable UUID id,
            @Valid @RequestBody CreateEscalationRequest request,
            @CurrentUser JwtPrincipal currentUser,
            HttpServletRequest httpRequest) {
        ComplaintEscalationDto created = complaintApplicationService.createEscalation(
                complaintApiMapper.toEscalationCreateRequest(id, request, currentUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(created, null, RequestContextUtils.resolveRequestId(httpRequest)));
    }

    @GetMapping("/{id}/escalations")
    @Operation(summary = "List complaint escalations")
    public ApiResponse<List<ComplaintEscalationDto>> listEscalations(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.listEscalations(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Duplicates ---

    @GetMapping("/{id}/duplicates")
    @Operation(summary = "List duplicate links for primary complaint")
    public ApiResponse<List<ComplaintDuplicateDto>> listDuplicates(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.listDuplicates(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }

    // --- Merges ---

    @GetMapping("/{id}/merges")
    @Operation(summary = "List merge records for surviving complaint")
    public ApiResponse<List<ComplaintMergeDto>> listMerges(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(
                complaintApplicationService.listMerges(id),
                null,
                RequestContextUtils.resolveRequestId(httpRequest));
    }
}
