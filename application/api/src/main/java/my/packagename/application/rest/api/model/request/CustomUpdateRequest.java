package my.packagename.application.rest.api.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CustomUpdateRequest {

    @NotEmpty
    @Size(max = 20)
    private String name;
}
