package my.packagename.model.mapper;

import my.packagename.model.CustomDTO;
import my.packagename.repository.domain.CustomEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CustomDTOMapper {

    @Mappings({
            @Mapping(target = "defaultDate", expression = "java(new java.util.Date())")
    })
    CustomDTO toCustomModelDTO(CustomEntity customEntity);
}
