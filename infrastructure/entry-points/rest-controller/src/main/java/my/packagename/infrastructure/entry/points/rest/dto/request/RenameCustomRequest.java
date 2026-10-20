package my.packagename.infrastructure.entry.points.rest.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Blank-but-present names pass here on purpose: the domain owns that rule and answers 422. */
public record RenameCustomRequest(@NotNull @Size(max = 20) String name) {}
