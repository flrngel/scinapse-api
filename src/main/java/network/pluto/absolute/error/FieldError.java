package network.pluto.absolute.error;

import lombok.Data;

@Data
public class FieldError {
    private String field;
    private Object rejectedValue;
    private String code;
    private String message;
}
