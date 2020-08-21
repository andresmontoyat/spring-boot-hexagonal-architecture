package my.packagename.rest.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CustomResponse {

    private String name;

    private Date defaultDate;
}
