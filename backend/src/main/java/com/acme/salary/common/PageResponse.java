package com.acme.salary.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A stable, explicit pagination shape for API responses. Spring Data's {@link Page} is not
 * meant to be serialized directly (its JSON shape isn't a public contract), so controllers
 * map to this instead.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last) {

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast());
    }
}
