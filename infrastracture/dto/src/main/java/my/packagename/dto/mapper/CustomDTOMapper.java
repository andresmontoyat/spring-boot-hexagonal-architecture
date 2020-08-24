package my.packagename.dto.mapper;

import my.packagename.domain.CustomEntity;
import my.packagename.dto.CustomDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomDTOMapper {

    CustomDTO toCustomModelDTO(CustomEntity customEntity);
}
