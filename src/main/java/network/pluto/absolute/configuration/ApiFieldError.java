package network.pluto.absolute.configuration;

import lombok.Data;

@Data
public class ApiFieldError {
    private String field;
    private Object rejectedValue;
    private String code;
    private String message;
}
