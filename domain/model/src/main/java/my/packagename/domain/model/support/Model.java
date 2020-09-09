package my.packagename.domain.model.support;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Model<ID> {

    private ID id;

}
