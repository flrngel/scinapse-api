package io.scinapse.api.error;

import io.scinapse.api.dto.response.Error;
import io.scinapse.api.dto.response.Response;
import io.scinapse.api.util.ErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
@Controller
public class GlobalErrorController extends ResponseEntityExceptionHandler implements ErrorController {

    private static final String ERROR_PATH = "/error";

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @RequestMapping(ERROR_PATH)
    @ResponseBody
    public ResponseEntity error(HttpServletRequest request) {
        HttpStatus status = ErrorUtils.extractStatus(request);
        Throwable throwable = ErrorUtils.extractError(request);
        String path = ErrorUtils.extractPath(request);

        if (throwable != null) {
            logFallbackError(status, throwable.getMessage(), path, throwable);

            Error error = Error.of(path, status, throwable);
            return new ResponseEntity<>(Response.error(error), status);

        } else {
            String message = ErrorUtils.extractErrorMessage(status, request);
            logFallbackError(status, message, path, null);

            Error error = Error.of(path, status, message);
            return new ResponseEntity<>(Response.error(error), status);
        }
    }

    @ExceptionHandler({ MultipartException.class })
    public ResponseEntity handleMultipartException(HttpServletRequest request, Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        Error error = Error.of(request.getRequestURI(), status, ex);
        return new ResponseEntity<>(Response.error(error), status);
    }

    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    public ResponseEntity handleAuthException(HttpServletRequest request, Exception ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorUtils.logError(status, request, ex);

        Error error = Error.of(request.getRequestURI(), status, ex);
        return new ResponseEntity<>(Response.error(error), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(HttpServletRequest request, Exception ex) {
        HttpStatus status = ErrorUtils.extractStatus(ex);

        ErrorUtils.logError(status, request, ex);

        Error error = Error.of(request.getRequestURI(), status, ex);
        return new ResponseEntity<>(Response.error(error), status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (request instanceof ServletWebRequest) {
            HttpServletRequest servletRequest = ((ServletWebRequest) request).getRequest();

            ErrorUtils.logError(status, servletRequest, ex);

            Error error = Error.of(servletRequest.getRequestURI(), status, ex);
            return new ResponseEntity<>(Response.error(error), headers, status);
        }

        ErrorUtils.logError(status, null, ex);

        Error error = Error.of(null, status, ex);
        return new ResponseEntity<>(Response.error(error), headers, status);
    }

    private void logFallbackError(HttpStatus status, String message, String path, Throwable error) {
        if (!status.is5xxServerError()) {
            return;
        }
        log.error("Resolved fallback exception:[ {} ] | Request URI:[ {} ]", message, path, error);
    }

}
