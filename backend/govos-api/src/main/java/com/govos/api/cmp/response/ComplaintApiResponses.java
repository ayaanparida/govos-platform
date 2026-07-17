package com.govos.api.cmp.response;

/**
 * API responses reuse domain DTOs from {@code com.govos.cmp.dto}.
 * <p>
 * Controllers return {@code ApiResponse<ComplaintDto>}, {@code ApiResponse<ComplaintCommentDto>}, etc.
 * This package is reserved for future API-specific response shaping when required.
 */
final class ComplaintApiResponses {
    private ComplaintApiResponses() {
    }
}
