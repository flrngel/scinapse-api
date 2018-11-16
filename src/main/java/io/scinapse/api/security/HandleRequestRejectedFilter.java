package io.scinapse.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HandleRequestRejectedFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RequestRejectedException e) {
            // catch RequestRejectedException because it generates 5xx status response.
            // user should be warned with 4xx status response if they use non-normalized request.
            // see StrictHttpFirewall
            log.warn("The request was rejected. Remote Host: {}, User Agent: {}, Request URL: {}",
                    request.getRemoteHost(),
                    request.getHeader(HttpHeaders.USER_AGENT),
                    request.getRequestURL());
            request.setAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE, e);
            response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

}
