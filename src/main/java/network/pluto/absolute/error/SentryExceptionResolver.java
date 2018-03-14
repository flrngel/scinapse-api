package network.pluto.absolute.error;

import io.sentry.Sentry;
import org.springframework.core.Ordered;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
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
                ex instanceof MethodArgumentNotValidException ||
                ex instanceof BadRequestException ||
                ex instanceof AuthenticationException) {

            // will not send exception event to sentry
            return null;
        }

        // Below exception's getMessage() is not much helpful for debug. Converting to meaningful message.
        if (ex instanceof HttpClientErrorException || ex instanceof HttpServerErrorException) {
            HttpStatusCodeException restEx = (HttpStatusCodeException) ex;
            RestClientException convertedEx = new RestClientException(restEx.getResponseBodyAsString(), ex);
            Sentry.capture(convertedEx);
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
