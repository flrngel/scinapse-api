package io.scinapse.api.error;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldError {

    private String field;
    private Object rejectedValue;
    private String code;
    private String message;

}
