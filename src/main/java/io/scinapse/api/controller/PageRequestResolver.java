package io.scinapse.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class PageRequestResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_MAX_PAGE = 100;
    private static final int DEFAULT_MAX_PAGE_SIZE = 10;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PageRequest.class.equals(parameter.getParameterType());
    }

    @Override
    public PageRequest resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String pageString = StringUtils.trimToNull(webRequest.getParameter("page"));
        int page = parseAndApplyBoundaries(pageString, DEFAULT_MAX_PAGE);

        String sort = StringUtils.trimToNull(webRequest.getParameter("sort"));

        return new PageRequest(page, DEFAULT_MAX_PAGE_SIZE, sort);
    }

    private int parseAndApplyBoundaries(String parameter, int upper) {
        try {
            int parsed = Integer.parseInt(parameter);
            return parsed < 0 ? 0 : parsed > upper ? upper : parsed;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
