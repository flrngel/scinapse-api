package io.scinapse.api.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
@Setter
public class Error {

    public static final Integer UNEXPECTED_ERROR_STATUS_CODE = 999;
    public static final String UNEXPECTED_ERROR = "Unexpected Error";

    private OffsetDateTime timestamp;
    private Integer status;
    private String reason;
    private String exception;
    private String message;
    private Object errors;
    private String path;

    private Error(HttpServletRequest request) {
        this.timestamp = OffsetDateTime.now();
        setStatus(request);
        setErrorDetail(request);
        setPath(request);
    }

    private Error(HttpServletRequest request, Exception e, HttpStatus status) {
        this.timestamp = OffsetDateTime.now();
        this.status = status.value();
        this.reason = status.getReasonPhrase();
        this.exception = e.getClass().getName();
        this.message = e.getMessage();
        this.path = request.getRequestURI();
    }

    public static Error of(HttpServletRequest request) {
        return new Error(request);
    }

    public static Error of(HttpServletRequest request, Exception e, HttpStatus status) {
        return new Error(request, e, status);
    }

    private void setStatus(HttpServletRequest request) {
        Integer statusCode = getAttribute(request, RequestDispatcher.ERROR_STATUS_CODE, Integer.class);
        if (statusCode == null) {
            this.status = UNEXPECTED_ERROR_STATUS_CODE;
            this.reason = UNEXPECTED_ERROR;
            return;
        }

        try {
            HttpStatus status = HttpStatus.valueOf(statusCode);
            this.status = status.value();
            this.reason = status.getReasonPhrase();
        } catch (Exception ex) {
            this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            this.reason = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        }
    }

    private void setErrorDetail(HttpServletRequest request) {
        Throwable error = getError(request);
        if (error != null) {
            this.exception = error.getClass().getName();
            this.message = getErrorMessage(error);
        }

        if (StringUtils.isBlank(this.message)) {
            this.message = getErrorMessage(request);
        }
    }

    private String getErrorMessage(Throwable throwable) {
        if (isInternalServerError()) {
            return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        }

        BindingResult result = extractBindingResult(throwable);
        if (result != null && result.getErrorCount() > 0) {
            this.errors = result.getAllErrors();
            return "Validation failed for object='" + result.getObjectName() + "'. Error count: " + result.getErrorCount();
        }

        return throwable.getMessage();
    }

    private BindingResult extractBindingResult(Throwable error) {
        if (error instanceof BindingResult) {
            return (BindingResult) error;
        }
        if (error instanceof MethodArgumentNotValidException) {
            return ((MethodArgumentNotValidException) error).getBindingResult();
        }
        return null;
    }

    private boolean isInternalServerError() {
        return status == null || status >= 500;
    }

    private String getErrorMessage(HttpServletRequest request) {
        String message = getAttribute(request, RequestDispatcher.ERROR_MESSAGE, String.class);
        return StringUtils.defaultIfBlank(message, "No message available.");
    }

    private Throwable getError(HttpServletRequest request) {
        Throwable exception = getAttribute(request, DispatcherServlet.EXCEPTION_ATTRIBUTE, Throwable.class);
        if (exception == null) {
            exception = getAttribute(request, RequestDispatcher.ERROR_EXCEPTION, Throwable.class);
        }

        if (exception == null) {
            return null;
        }

        while (exception instanceof ServletException && exception.getCause() != null) {
            exception = exception.getCause();
        }
        return exception;
    }

    private void setPath(HttpServletRequest request) {
        this.path = getAttribute(request, RequestDispatcher.ERROR_REQUEST_URI, String.class);
    }

    private <T> T getAttribute(HttpServletRequest request, String name, Class<T> type) {
        Object attribute = request.getAttribute(name);
        if (type.isInstance(attribute)) {
            return type.cast(attribute);
        }
        return null;
    }

}
