package io.scinapse.api.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtils {

    public static boolean isBot(HttpServletRequest request) {
        String userAgentHeaderString = request.getHeader("User-Agent");
        return StringUtils.containsIgnoreCase(userAgentHeaderString, "googlebot");
    }

}
