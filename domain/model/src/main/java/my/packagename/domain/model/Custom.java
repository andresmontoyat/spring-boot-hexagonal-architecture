package my.packagename.domain.model;

import lombok.Data;
import my.packagename.domain.model.support.Model;

@Data
public class Custom extends Model<Long> {

    private String name;
}
