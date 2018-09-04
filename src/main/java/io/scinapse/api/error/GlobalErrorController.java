package io.scinapse.api.error;

import io.scinapse.api.dto.response.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

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

}
