package network.pluto.absolute.configuration;

import lombok.extern.slf4j.Slf4j;
import network.pluto.absolute.security.TokenExpiredException;
import network.pluto.absolute.security.TokenInvalidException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackages = "network.pluto.absolute")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TokenExpiredException.class)
    protected ResponseEntity<Object> handleTokenExpiredException(TokenExpiredException ex) {
        return buildResponseEntity(new ApiError(HttpStatus.UNAUTHORIZED, ex.getLocalizedMessage() + ": " + ex.getReason()));
    }

    @ExceptionHandler(TokenInvalidException.class)
    protected ResponseEntity<Object> handleTokenInvalidException(TokenInvalidException ex) {
        return buildResponseEntity(new ApiError(HttpStatus.UNAUTHORIZED, ex.getLocalizedMessage() + ": " + ex.getReason()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        if (ex.getCause() instanceof ConstraintViolationException) {
            log.error("Database error: {}", ex.getCause().getLocalizedMessage(), ex.getCause());
            return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, "Database error", ex.getCause()));
        }
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ApiFieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    ApiFieldError fieldError = new ApiFieldError();
                    fieldError.setField(error.getField());

                    if (!"password".equals(error.getField())) {
                        fieldError.setRejectedValue(error.getRejectedValue());
                    }

                    fieldError.setCode(error.getCode());
                    fieldError.setMessage(error.getDefaultMessage());
                    return fieldError;
                })
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed");
        apiError.setFieldErrors(fieldErrors);

        return buildResponseEntity(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage()));
    }

    @ExceptionHandler(Throwable.class)
    protected ResponseEntity<Object> handleDefaultException(Throwable ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getLocalizedMessage(), ex);
        return buildResponseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
