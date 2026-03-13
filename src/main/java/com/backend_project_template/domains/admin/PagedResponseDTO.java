package com.backend_project_template.domains.admin;

import java.util.List;

/**
 * Generic DTO for paginated responses
 */
public record PagedResponseDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
    public static <T> PagedResponseDTO<T> of(
            List<T> content,
            int page,
            int size,
            long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean first = page == 0;
        boolean last = page >= totalPages - 1;
        return new PagedResponseDTO<>(content, page, size, totalElements, totalPages, first, last);
    }
}
