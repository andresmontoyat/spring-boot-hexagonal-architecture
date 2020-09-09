package my.packagename.commons.util.ex;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class Message {

    @JsonProperty("code")
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd hh:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    public Message(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
