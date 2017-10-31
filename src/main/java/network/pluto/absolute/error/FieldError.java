package network.pluto.absolute.error;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class FieldError {
    private String field;
    private Object rejectedValue;
    private String code;
    private String message;
}
