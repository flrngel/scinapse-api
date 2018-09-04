package io.scinapse.api.security.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.DispatcherServlet;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;


@RunWith(SpringRunner.class)
public class RestAccessDeniedHandlerTest {

    private RestAccessDeniedHandler handler;

    @MockBean
    private MockHttpServletRequest request;

    @MockBean
    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        this.handler = new RestAccessDeniedHandler();
    }

    @Test
    public void handle() throws Exception {
        AccessDeniedException exception = new AccessDeniedException("access denied");

        handler.handle(request, response, exception);

        verify(request, only()).setAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE, exception);
        verify(response, only()).sendError(HttpStatus.UNAUTHORIZED.value(), "access denied");
    }

}