package network.pluto.absolute.security.jwt;

import network.pluto.absolute.security.TokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenHelper tokenHelper;

    @Autowired
    public JwtAuthenticationSuccessHandler(TokenHelper tokenHelper) {
        this.tokenHelper = tokenHelper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = (String) authentication.getCredentials();

        String refreshedToken = tokenHelper.refreshToken(token);
        tokenHelper.addCookie(response, refreshedToken);

        clearAuthenticationAttributes(request);
    }
}
