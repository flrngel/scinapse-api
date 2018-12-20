package io.scinapse.api.error;

import io.scinapse.api.dto.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Controller
public class GlobalErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

    private static final String ERROR_PATH = "/error";

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @RequestMapping(ERROR_PATH)
    @ResponseBody
    public Response error(HttpServletRequest request) {
        return Response.error(request);
    }

    @ExceptionHandler({ MultipartException.class })
    public Response handleMultipartException(HttpServletRequest request, Exception e) {
        return Response.error(request, e, HttpStatus.BAD_REQUEST);
    }

}
