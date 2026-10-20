package my.packagename.infrastructure.entry.points.rest.mapper;

import java.util.UUID;
import my.packagename.application.command.CreateCustomCommand;
import my.packagename.application.command.RenameCustomCommand;
import my.packagename.application.query.PageResult;
import my.packagename.domain.model.Custom;
import my.packagename.domain.model.CustomId;
import my.packagename.infrastructure.entry.points.rest.dto.request.CreateCustomRequest;
import my.packagename.infrastructure.entry.points.rest.dto.request.RenameCustomRequest;
import my.packagename.infrastructure.entry.points.rest.dto.response.CustomResponse;
import my.packagename.infrastructure.entry.points.rest.dto.response.PageResponse;

public final class CustomRestMapper {

  private CustomRestMapper() {}

  public static CreateCustomCommand toCommand(CreateCustomRequest request) {
    return new CreateCustomCommand(request.name());
  }

  public static RenameCustomCommand toCommand(UUID id, RenameCustomRequest request) {
    return new RenameCustomCommand(new CustomId(id), request.name());
  }

  public static CustomResponse toResponse(Custom custom) {
    return new CustomResponse(custom.id().value(), custom.name().value(), custom.version());
  }

  public static PageResponse<CustomResponse> toResponse(PageResult<Custom> page) {
    return new PageResponse<>(
        page.items().stream().map(CustomRestMapper::toResponse).toList(),
        page.page(),
        page.size(),
        page.totalElements(),
        page.totalPages());
  }
}
