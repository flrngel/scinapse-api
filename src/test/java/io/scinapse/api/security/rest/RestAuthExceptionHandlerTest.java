package io.scinapse.api.security.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class RestAuthExceptionHandlerTest {

    private RestAuthExceptionHandler handler;

    @MockBean
    private MockHttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        this.handler = new RestAuthExceptionHandler();
    }

    @Test
    public void commence() throws Exception {
        AuthenticationException exception = new BadCredentialsException("bad credentials");

        handler.commence(null, response, exception);

        verify(response, only()).sendError(HttpStatus.UNAUTHORIZED.value(), "bad credentials");
    }

}