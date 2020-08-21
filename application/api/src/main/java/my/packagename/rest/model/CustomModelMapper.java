package my.packagename.rest.model;

import my.packagename.model.CustomDTO;
import my.packagename.rest.model.response.CustomResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomModelMapper {

    CustomResponse toCustomResponse(CustomDTO customDTO);
}