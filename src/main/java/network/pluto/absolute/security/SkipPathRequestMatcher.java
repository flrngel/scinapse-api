package network.pluto.absolute.security;

import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class SkipPathRequestMatcher implements RequestMatcher {

    private OrRequestMatcher skipMatcher;
    private RequestMatcher processingMatcher;

    public SkipPathRequestMatcher(List<RequestMatcher> skipPaths, RequestMatcher processingPath) {
        skipMatcher = new OrRequestMatcher(skipPaths);
        processingMatcher = processingPath;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if (skipMatcher.matches(request)) {
            return false;
        }
        return processingMatcher.matches(request);
    }
}
