package com.govos.api.common.pagination;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses sort query parameters into Spring Data {@link Sort} objects.
 * <p>
 * Format: {@code field,direction} with optional multi-sort separated by {@code ;}.
 * Example: {@code createdDate,desc;code,asc}
 */
public final class SortParser {

    private static final Sort DEFAULT_SORT = Sort.unsorted();

    private SortParser() {
    }

    public static Sort parse(String sortParam) {
        return parse(sortParam, DEFAULT_SORT);
    }

    public static Sort parse(String sortParam, Sort defaultSort) {
        if (sortParam == null || sortParam.isBlank()) {
            return defaultSort;
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (String token : sortParam.split(";")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String[] parts = trimmed.split(",");
            String property = parts[0].trim();
            if (property.isEmpty()) {
                continue;
            }

            Sort.Direction direction = Sort.Direction.ASC;
            if (parts.length > 1) {
                direction = Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.ASC);
            }
            orders.add(new Sort.Order(direction, property));
        }

        return orders.isEmpty() ? defaultSort : Sort.by(orders);
    }
}
