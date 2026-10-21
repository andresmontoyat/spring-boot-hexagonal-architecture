package my.packagename.infrastructure.entry.points.rest.dto.response;

import java.util.List;

public record PageResponse<T>(
    List<T> items, int page, int size, long totalElements, long totalPages) {}
