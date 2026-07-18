package com.govos.api.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the correlation / request identifier from the current HTTP request.
 */
public final class RequestContextUtils {

    private RequestContextUtils() {
    }

    public static String resolveRequestId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attribute = request.getAttribute(ApiConstants.REQUEST_ID_ATTRIBUTE);
        if (attribute instanceof String requestId && !requestId.isBlank()) {
            return requestId;
        }
        return request.getHeader(ApiConstants.REQUEST_ID_HEADER);
    }
}
