package io.scinapse.api.controller;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class PageRequestResolverTest {

    private PageRequestResolver resolver = new PageRequestResolver();

    @Test
    public void default_page_test() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);

        PageRequest pageRequest = resolver.resolveArgument(null, null, servletWebRequest, null);

        assertThat(pageRequest).isNotNull();
        assertThat(pageRequest.getSize()).isEqualTo(10);
        assertThat(pageRequest.getPage()).isEqualTo(0);
    }

}