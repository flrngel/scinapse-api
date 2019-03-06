package io.scinapse.api.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class PageRequestResolver implements HandlerMethodArgumentResolver {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int DEFAULT_PAGE_NUMBER = 0;
    private static final int MAX_PAGE_SIZE = 30;
    private static final int MAX_ELEMENT_SIZE = 5000;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PageRequest.class.equals(parameter.getParameterType());
    }

    @Override
    public PageRequest resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String sizeString = StringUtils.trimToNull(webRequest.getParameter("size"));
        int size = parseAndApplyBoundaries(sizeString, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);

        String pageString = StringUtils.trimToNull(webRequest.getParameter("page"));
        int page = parseAndApplyBoundaries(pageString, DEFAULT_PAGE_NUMBER, MAX_ELEMENT_SIZE / size);

        String sort = StringUtils.trimToNull(webRequest.getParameter("sort"));

        return new PageRequest(page, size, sort);
    }

    private int parseAndApplyBoundaries(String parameter, int defaultValue, int upper) {
        try {
            int parsed = Integer.parseInt(parameter);
            return parsed < 0 ? defaultValue : parsed > upper ? upper : parsed;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

}
