package my.packagename.infrastructure.entry.points.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import my.packagename.application.port.inbound.CreateCustomPort;
import my.packagename.application.port.inbound.DeleteCustomPort;
import my.packagename.application.port.inbound.FindCustomPort;
import my.packagename.application.port.inbound.ListCustomsPort;
import my.packagename.application.port.inbound.RenameCustomPort;
import my.packagename.application.query.PageQuery;
import my.packagename.domain.model.CustomId;
import my.packagename.infrastructure.entry.points.rest.dto.request.CreateCustomRequest;
import my.packagename.infrastructure.entry.points.rest.dto.request.RenameCustomRequest;
import my.packagename.infrastructure.entry.points.rest.dto.response.CustomResponse;
import my.packagename.infrastructure.entry.points.rest.dto.response.PageResponse;
import my.packagename.infrastructure.entry.points.rest.mapper.CustomRestMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "Customs")
@RestController
@RequestMapping("/api/v1/customs")
public class CustomController {

  /** Keycloak client roles platform-reader / platform-writer, mapped by the JWT converter. */
  static final String CAN_READ = "hasAnyRole('READER', 'WRITER')";

  static final String CAN_WRITE = "hasRole('WRITER')";

  private final CreateCustomPort createCustom;
  private final FindCustomPort findCustom;
  private final ListCustomsPort listCustoms;
  private final RenameCustomPort renameCustom;
  private final DeleteCustomPort deleteCustom;

  public CustomController(
      CreateCustomPort createCustom,
      FindCustomPort findCustom,
      ListCustomsPort listCustoms,
      RenameCustomPort renameCustom,
      DeleteCustomPort deleteCustom) {
    this.createCustom = createCustom;
    this.findCustom = findCustom;
    this.listCustoms = listCustoms;
    this.renameCustom = renameCustom;
    this.deleteCustom = deleteCustom;
  }

  @GetMapping
  @PreAuthorize(CAN_READ)
  public PageResponse<CustomResponse> list(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(PageQuery.MAX_SIZE) int size) {
    return CustomRestMapper.toResponse(listCustoms.execute(new PageQuery(page, size)));
  }

  @GetMapping("/{id}")
  @PreAuthorize(CAN_READ)
  public CustomResponse find(@PathVariable UUID id) {
    return CustomRestMapper.toResponse(findCustom.execute(new CustomId(id)));
  }

  @PostMapping
  @PreAuthorize(CAN_WRITE)
  public ResponseEntity<CustomResponse> create(@Valid @RequestBody CreateCustomRequest request) {
    CustomResponse created =
        CustomRestMapper.toResponse(createCustom.execute(CustomRestMapper.toCommand(request)));
    return ResponseEntity.created(locationOf(created)).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize(CAN_WRITE)
  public CustomResponse rename(
      @PathVariable UUID id, @Valid @RequestBody RenameCustomRequest request) {
    return CustomRestMapper.toResponse(
        renameCustom.execute(CustomRestMapper.toCommand(id, request)));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(CAN_WRITE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID id) {
    deleteCustom.execute(new CustomId(id));
  }

  private static URI locationOf(CustomResponse created) {
    return ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(created.id())
        .toUri();
  }
}
