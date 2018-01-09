package network.pluto.absolute.error;

import io.sentry.Sentry;
import org.springframework.core.Ordered;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SentryExceptionResolver implements HandlerExceptionResolver, Ordered {

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        if (ex instanceof ResourceNotFoundException ||
                ex instanceof BadRequestException ||
                ex instanceof AuthenticationException) {

            // will not send exception event to sentry
            return null;
        }

        // send exception event
        Sentry.capture(ex);

        // null = run other HandlerExceptionResolvers to actually handle the exception
        return null;
    }

    @Override
    public int getOrder() {

        // ensure this resolver runs first so that all exceptions are reported
        return Integer.MIN_VALUE;
    }
}
