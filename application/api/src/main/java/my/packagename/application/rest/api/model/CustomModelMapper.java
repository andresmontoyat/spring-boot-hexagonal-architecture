package my.packagename.application.rest.api.model;

import my.packagename.application.rest.api.model.request.CustomCreateRequest;
import my.packagename.application.rest.api.model.request.CustomUpdateRequest;
import my.packagename.application.rest.api.model.response.CustomResponse;
import my.packagename.domain.model.Custom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomModelMapper {
    Custom toCustom(CustomCreateRequest customCreateRequest);

    @Mappings({
            @Mapping(target = "id", source = "id"),
            @Mapping(target = "name", source = "customUpdateRequest.name")
    })
    Custom toCustom(Long id, CustomUpdateRequest customUpdateRequest);

    CustomResponse toCustomResponse(Custom custom);

    List<CustomResponse> toCustomResponses(List<Custom> customs);
}
