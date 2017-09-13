package network.pluto.absolute.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import network.pluto.absolute.service.LoginUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenHelper tokenHelper;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthenticationSuccessHandler(TokenHelper tokenHelper, ObjectMapper objectMapper) {
        this.tokenHelper = tokenHelper;
        this.objectMapper = objectMapper;
    }

    @Value("${jwt.cookie}")
    private String cookie;

    @Value("${jwt.expires-in}")
    private int expireIn;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        clearAuthenticationAttributes(request);
        LoginUserDetails user = (LoginUserDetails) authentication.getPrincipal();

        String jws = tokenHelper.generateToken(user.getMember());

        Cookie authCookie = new Cookie(cookie, jws);
        authCookie.setPath("/");
        authCookie.setHttpOnly(true);
        authCookie.setMaxAge(expireIn);
        response.addCookie(authCookie);

        TokenState tokenState = new TokenState(jws, System.currentTimeMillis() + expireIn * 1000);
        String jwtResponse = objectMapper.writeValueAsString(tokenState);

        response.setContentType("application/json");
        response.getWriter().write(jwtResponse);
    }
}
