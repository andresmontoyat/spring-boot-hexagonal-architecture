package my.packagename.infrastructure.entry.points.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomRequest(@NotBlank @Size(max = 20) String name) {}
