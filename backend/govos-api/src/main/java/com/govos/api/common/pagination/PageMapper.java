package com.govos.api.common.pagination;

import org.springframework.data.domain.Page;

import java.util.function.Function;

/**
 * Maps Spring Data {@link Page} instances to API {@link PageResponse} records.
 */
public final class PageMapper {

    private PageMapper() {
    }

    public static <T> PageResponse<T> toPageResponse(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSort().toString());
    }

    public static <S, T> PageResponse<T> toPageResponse(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSort().toString());
    }
}
