package my.packagename.infrastructure.entry.points.rest.dto.response;

import java.util.UUID;

public record CustomResponse(UUID id, String name, long version) {}
