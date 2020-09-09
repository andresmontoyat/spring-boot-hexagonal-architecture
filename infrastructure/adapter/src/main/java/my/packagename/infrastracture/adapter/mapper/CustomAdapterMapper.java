package my.packagename.infrastracture.adapter.mapper;

import my.packagename.domain.model.Custom;
import my.packagename.infrastracture.adapter.mapper.support.JpaMapper;
import my.packagename.infrastracture.repository.entities.CustomEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class CustomAdapterMapper extends JpaMapper<Custom, CustomEntity> {

    public abstract CustomEntity toCustomEntity(Custom custom);

    public abstract Custom toCustom(CustomEntity customEntity);

    public abstract List<Custom> toCustoms(List<CustomEntity> customEntity);
}
