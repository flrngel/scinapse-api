package io.scinapse.api.error;

import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus
public class ExternalApiCallException extends RuntimeException {

    public ExternalApiCallException() {
        this("Error Occurs while calling external API");
    }

    public ExternalApiCallException(String message) {
        super(message);
    }

}
