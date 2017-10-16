package network.pluto.absolute.error;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
        Map<String, Object> errorAttributes = new LinkedHashMap<>();
        errorAttributes.put("timestamp", LocalDateTime.now());
        addStatus(errorAttributes, requestAttributes);
        addPath(errorAttributes, requestAttributes);
        addErrorDetails(errorAttributes, requestAttributes);
        return errorAttributes;
    }

    private void addErrorDetails(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        if (errorAttributes.get("status").equals(500)) {
            errorAttributes.put("message", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            return;
        }

        Throwable error = getError(requestAttributes);

        // add field error details
        if (error instanceof MethodArgumentNotValidException) {
            errorAttributes.put("fieldErrors", getFieldErrors((MethodArgumentNotValidException) error));
        }

        // add error message
        Object message = getAttribute(requestAttributes, "javax.servlet.error.message");
        errorAttributes.put("message", message);
    }

    private List<FieldError> getFieldErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    FieldError fieldError = new FieldError();
                    fieldError.setField(error.getField());

                    if (!"password".equals(error.getField())) {
                        fieldError.setRejectedValue(error.getRejectedValue());
                    }

                    fieldError.setCode(error.getCode());
                    fieldError.setMessage(error.getDefaultMessage());
                    return fieldError;
                })
                .collect(Collectors.toList());
    }

    private void addPath(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        String path = getAttribute(requestAttributes, "javax.servlet.error.request_uri");
        if (path != null) {
            errorAttributes.put("path", path);
        }
    }

    private void addStatus(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
        Integer status = getAttribute(requestAttributes, "javax.servlet.error.status_code");
        if (status == null) {
            errorAttributes.put("status", 999);
            errorAttributes.put("error", "None");
            return;
        }
        errorAttributes.put("status", status);
        try {
            errorAttributes.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        } catch (Exception ex) {
            // Unable to obtain a reason
            errorAttributes.put("error", "Http Status " + status);
        }
    }

    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }
}
